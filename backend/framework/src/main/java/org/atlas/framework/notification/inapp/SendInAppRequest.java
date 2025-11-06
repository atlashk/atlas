package org.atlas.framework.notification.inapp;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SendInAppRequest {

  private Integer receiverUserId;
  private Payload payload;

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Payload {

    private String message;
    private Date notifiedAt;
  }
}
