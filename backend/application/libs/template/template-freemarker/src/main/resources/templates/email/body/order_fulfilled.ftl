<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Order Confirmation</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      line-height: 1.6;
      color: #333;
    }

    .header {
      background-color: #f8f8f8;
      padding: 10px;
      text-align: center;
      font-size: 20px;
      font-weight: bold;
    }

    .content {
      margin: 20px;
    }

    .order-summary {
      border-collapse: collapse;
      width: 100%;
      margin-top: 20px;
    }

    .order-summary th, .order-summary td {
      border: 1px solid #ddd;
      padding: 8px;
      text-align: left;
    }

    .order-summary th {
      background-color: #f2f2f2;
    }

    .total {
      font-weight: bold;
    }
  </style>
</head>
<body>
<div class="header">Order Confirmation</div>

<div class="content">
  <p>Dear ${order.user.firstName} ${order.user.lastName},</p>

  <p>Thank you for your order! Your order <strong>${order.id}</strong>, placed on
    <strong><#if order.createdAt??>${order.createdAt.format("yyyy-MM-dd HH:mm:ss")}<#else>N/A</#if></strong>, was confirmed.</p>

  <table class="order-summary">
    <thead>
    <tr>
      <th>Product ID</th>
      <th>Product Name</th>
      <th>Price</th>
      <th>Quantity</th>
      <th>Subtotal</th>
    </tr>
    </thead>
    <tbody>
    <#list order.orderItems as orderItem>
      <tr>
        <td>${orderItem.product.id}</td>
        <td>${orderItem.product.name}</td>
        <td>${orderItem.product.price?string("$#,##0.00")}</td>
        <td>${orderItem.quantity}</td>
        <td>${(orderItem.product.price * orderItem.quantity)?string("$#,##0.00")}</td>
      </tr>
    </#list>
    </tbody>
    <tfoot>
    <tr>
      <td colspan="3"></td>
      <td class="total">Total Amount</td>
      <td class="total">${order.amount?string("$#,##0.00")}</td>
    </tr>
    </tfoot>
  </table>

  <#if order.address??>
    <h3>Delivery Address</h3>
    <table class="order-summary">
      <tbody>
      <tr>
        <td><strong>Street</strong></td>
        <td>${order.address.street!"N/A"}</td>
      </tr>
      <tr>
        <td><strong>City</strong></td>
        <td>${order.address.city!"N/A"}</td>
      </tr>
      <tr>
        <td><strong>Country</strong></td>
        <td>${order.address.country!"N/A"}</td>
      </tr>
      <tr>
        <td><strong>Postal Code</strong></td>
        <td>${order.address.postalCode!"N/A"}</td>
      </tr>
      </tbody>
    </table>
  </#if>

  <#if order.payment??>
    <h3>Payment Information</h3>
    <table class="order-summary">
      <tbody>
      <tr>
        <td><strong>Payment Gateway</strong></td>
        <td>${order.payment.paymentGatewayName!"N/A"}</td>
      </tr>
      <tr>
        <td><strong>Payment Method</strong></td>
        <td>${order.payment.paymentMethod!"N/A"}</td>
      </tr>
      <tr>
        <td><strong>Payment Details</strong></td>
        <td>${order.payment.paymentMethodDetails!"N/A"}</td>
      </tr>
      <tr>
        <td><strong>Transaction ID</strong></td>
        <td>${order.payment.transactionId!"N/A"}</td>
      </tr>
      </tbody>
    </table>
  </#if>

  <p>Best regards,</p>
  <p>Atlas</p>
</div>
</body>
</html>
