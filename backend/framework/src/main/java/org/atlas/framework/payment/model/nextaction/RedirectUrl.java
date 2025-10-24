package org.atlas.framework.payment.model.nextaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payment gateway should return URL like approval_url or 3ds_url. FE will navigate user to the
 * URL.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RedirectUrl implements NextAction {

  private String url;

  @Override
  public NextActionType getType() {
    return NextActionType.REDIRECT_URL;
  }
}
