package org.atlas.framework.paymentgateway.model.nextaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FE will navigate user to the mobile application via deeplink.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeepLink implements NextAction {

  private String url;

  @Override
  public NextActionType getType() {
    return NextActionType.DEEPLINK;
  }
}
