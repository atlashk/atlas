package org.atlas.notification.api.server.rest.front.model;

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
public class InAppNotificationResponse {

  private Integer id;
  private String message;
  private Date deliveredAt;
  private boolean read;
}
