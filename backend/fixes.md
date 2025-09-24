## Chiến lược xử lý webhook failure
### 1. Payment Status Polling Service
Tạo service để định kỳ kiểm tra trạng thái payment:

```
@DomainService
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusPollingService {

    private final PaymentRepository paymentRepository;
    private final PaymentRoutingService paymentRoutingService;
    private final ExternalMessagePublisherPort 
    externalMessagePublisherPort;
    private final ApplicationConfigPort applicationConfigPort;

    @Scheduled(fixedDelay = 30000) // Poll every 30 seconds
    public void pollPendingPayments() {
        List<PaymentEntity> pendingPayments = paymentRepository.
        findByStatusAndCreatedAtBefore(
            PaymentStatus.CREATED, 
            Instant.now().minus(5, ChronoUnit.MINUTES) // Payments 
            older than 5 minutes
        );

        for (PaymentEntity payment : pendingPayments) {
            try {
                checkPaymentStatus(payment);
            } catch (Exception e) {
                log.error("Error checking payment status for 
                paymentId={}", payment.getId(), e);
            }
        }
    }

    private void checkPaymentStatus(PaymentEntity payment) {
        PaymentGatewayPort gateway = paymentRoutingService.
        getPaymentGateway(payment.getMethod());
        
        // Call payment gateway API to check status
        PaymentStatusResponse statusResponse = gateway.
        getPaymentStatus(payment.getTransactionId());
        
        if (statusResponse.getStatus() != payment.getStatus()) {
            updatePaymentStatus(payment, statusResponse);
        }
    }

    private void updatePaymentStatus(PaymentEntity payment, 
    PaymentStatusResponse statusResponse) {
        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(statusResponse.getStatus());
        
        if (statusResponse.getStatus() == PaymentStatus.SUCCEEDED) {
            payment.setErrorCode(null);
            payment.setErrorMessage(null);
        } else if (statusResponse.getStatus() == PaymentStatus.
        FAILED) {
            payment.setErrorCode(statusResponse.getErrorCode());
            payment.setErrorMessage(statusResponse.getErrorMessage());
        }
        
        paymentRepository.update(payment);
        
        // Publish appropriate event
        publishPaymentStatusEvent(payment, oldStatus);
        
        log.info("Updated payment status: paymentId={}, oldStatus={}, 
        newStatus={}", 
            payment.getId(), oldStatus, payment.getStatus());
    }
}
```
### 2. Payment Gateway Status Check API
Thêm method vào `StripePaymentGatewayAdapter.java` :

```
// ... existing code ...
@Override
public PaymentStatusResponse getPaymentStatus(String transactionId) 
throws PaymentGatewayException {
    try {
        PaymentIntent paymentIntent = stripeService.
        retrievePaymentIntent(transactionId);
        
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setTransactionId(transactionId);
        
        switch (paymentIntent.getStatus()) {
            case "succeeded" -> response.setStatus(PaymentStatus.
            SUCCEEDED);
            case "requires_payment_method", "requires_confirmation", 
            "requires_action" -> 
                response.setStatus(PaymentStatus.CREATED);
            case "canceled" -> {
                response.setStatus(PaymentStatus.CANCELED);
                response.setCancellationReason(paymentIntent.
                getCancellationReason());
            }
            case "processing" -> response.setStatus(PaymentStatus.
            CREATED);
            default -> {
                response.setStatus(PaymentStatus.FAILED);
                if (paymentIntent.getLastPaymentError() != null) {
                    response.setErrorCode(paymentIntent.
                    getLastPaymentError().getCode());
                    response.setErrorMessage(paymentIntent.
                    getLastPaymentError().getMessage());
                }
            }
        }
        
        return response;
    } catch (StripeException e) {
        throw new PaymentGatewayException("Failed to retrieve payment 
        status", e);
    }
}
// ... existing code ...
```
### 3. Frontend Payment Status Checking
Tạo API endpoint để frontend có thể check trạng thái payment:

```
@RestController
@RequestMapping("/api/payments")
@Validated
@RequiredArgsConstructor
public class PaymentStatusController {

    private final PaymentStatusCheckUseCaseHandler 
    paymentStatusCheckUseCaseHandler;

    @GetMapping(value = "/{paymentId}/status", produces = MediaType.
    APPLICATION_JSON_VALUE)
    @Operation(summary = "Check payment status")
    public ApiResponseWrapper<PaymentStatusResponse> 
    checkPaymentStatus(
        @PathVariable("paymentId") Integer paymentId) throws 
        Exception {
        
        PaymentStatusResponse response = 
        paymentStatusCheckUseCaseHandler.handle(paymentId);
        return ApiResponseWrapper.success(response);
    }
}
```
### 4. Webhook Retry Mechanism
Cải thiện webhook handling với retry logic:

```
// ... existing code ...
public WebhookResponse handle(PaymentGateway paymentGateway,
    Map<String, Object> payload, Map<String, String> headers) {
    
    log.info("Received webhook event: paymentGateway={}, payload={}, 
    headers={}",
        paymentGateway, payload, headers);

    try {
        // Find payment gateway port implementation
        String paymentGatewayInstanceName = String.format
        ("%sPaymentGatewayAdapter",
            paymentGateway.name().toLowerCase());
        PaymentGatewayPort paymentGatewayPort = dependencyPort.
        getInstanceByName(
                paymentGatewayInstanceName, PaymentGatewayPort.class)
            .orElseThrow(() -> new DomainException(DomainError.
            PAYMENT_GATEWAY_NOT_SUPPORTED));

        WebhookResponse response = paymentGatewayPort.handleWebhook
        (payload, headers);
        
        if (response.getResponseStatus() == 200 && response.
        getPaymentResult() != null) {
            // Execute the remaining tasks asynchronously with retry
            AsyncUtil.executeAsyncWithRetry(new AsyncTask() {
                @Override
                public void run() {
                    processPaymentResult(response.getPaymentResult());
                }

                @Override
                public void onSuccess() {
                    log.info("Successfully processed webhook for 
                    paymentId={}", 
                        response.getPaymentResult().getPaymentId());
                }

                @Override
                public void onError(Throwable ex) {
                    log.error("Failed to process webhook for 
                    paymentId={}", 
                        response.getPaymentResult().getPaymentId(), 
                        ex);
                    // Could implement dead letter queue here
                }
            }, 3, 1000); // 3 retries with 1 second delay
        }

        return response;
    } catch (Exception e) {
        log.error("Error processing webhook", e);
        WebhookResponse errorResponse = new WebhookResponse();
        errorResponse.setResponseStatus(500);
        return errorResponse;
    }
}

private void processPaymentResult(PaymentResult paymentResult) {
    // Update payment entity
    PaymentEntity paymentEntity = paymentRepository.findById
    (paymentResult.getPaymentId())
        .orElseThrow(() -> new DomainException(DomainError.
        PAYMENT_NOT_FOUND));
    
    // ... rest of the processing logic ...
}
// ... existing code ...
```
### 5. Payment Timeout Handling
Thêm timeout mechanism:

```
@DomainService
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeoutService {

    private final PaymentRepository paymentRepository;
    private final ExternalMessagePublisherPort 
    externalMessagePublisherPort;
    private final ApplicationConfigPort applicationConfigPort;

    @Scheduled(fixedDelay = 60000) // Check every minute
    public void handleTimeoutPayments() {
        Instant timeoutThreshold = Instant.now().minus(30, ChronoUnit.
        MINUTES);
        
        List<PaymentEntity> timeoutPayments = paymentRepository.
        findByStatusAndCreatedAtBefore(
            PaymentStatus.CREATED, timeoutThreshold);

        for (PaymentEntity payment : timeoutPayments) {
            handleTimeoutPayment(payment);
        }
    }

    private void handleTimeoutPayment(PaymentEntity payment) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorCode("TIMEOUT");
        payment.setErrorMessage("Payment timed out after 30 minutes");
        paymentRepository.update(payment);

        // Publish timeout event
        Order order = buildOrderFromPayment(payment);
        PaymentFailedEvent event = new PaymentFailedEvent(
            applicationConfigPort.getApplicationName(), order);
        event.setErrorCode("TIMEOUT");
        event.setErrorMessage("Payment timed out");
        externalMessagePublisherPort.publish(event);

        log.warn("Payment timed out: paymentId={}, orderId={}", 
            payment.getId(), payment.getOrderId());
    }
}
```
### 6. Frontend Implementation
Trong frontend, implement polling mechanism:

```
export class PaymentService {
    async pollPaymentStatus(paymentId: number, maxAttempts: number = 
    60): Promise<PaymentStatus> {
        for (let attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                const response = await this.checkPaymentStatus
                (paymentId);
                
                if (response.status === 'SUCCEEDED' || 
                    response.status === 'FAILED' || 
                    response.status === 'CANCELED') {
                    return response;
                }
                
                // Wait 5 seconds before next poll
                await new Promise(resolve => setTimeout(resolve, 
                5000));
            } catch (error) {
                console.error('Error polling payment status:', error);
                await new Promise(resolve => setTimeout(resolve, 
                5000));
            }
        }
        
        throw new Error('Payment status polling timeout');
    }

    private async checkPaymentStatus(paymentId: number): 
    Promise<PaymentStatusResponse> {
        const response = await fetch(`/api/payments/${paymentId}/
        status`);
        return response.json();
    }
}
```
## Tóm tắt chiến lược
1. 1.
   Fix bug hiện tại trong WebhookHandler
2. 2.
   Polling service để định kỳ check trạng thái payment
3. 3.
   Payment status API để frontend có thể check
4. 4.
   Webhook retry mechanism với error handling
5. 5.
   Timeout handling cho payments quá lâu
6. 6.
   Frontend polling để đảm bảo UX tốt