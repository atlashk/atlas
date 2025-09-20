package org.atlas.framework.payment.model;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentResponse {

  private boolean success;
  private Map<String, Object> data;
  private String errorCode;
  private String errorMessage;
}
