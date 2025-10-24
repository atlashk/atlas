package org.atlas.framework.payment.model.nextaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The payment gateway should return the QR code as a string, e.g., a Base64 string. FE will render
 * the QR code for the user to scan with mobile application.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class QRCode implements NextAction {

  private String content;

  @Override
  public NextActionType getType() {
    return NextActionType.QR_CODE;
  }
}
