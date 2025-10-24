package org.atlas.framework.payment.model.nextaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FE will navigate user to the mobile application via deeplink.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DeepLink implements NextAction {

  private String url;

  @Override
  public NextActionType getType() {
    return NextActionType.DEEPLINK;
  }
}
