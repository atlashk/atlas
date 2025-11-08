package org.atlas.framework.storage.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class GetDownloadUrlRequest extends BaseRequest {

  private Duration ttl;

  public GetDownloadUrlRequest(String bucket, String objectKey, Duration ttl) {
    super(bucket, objectKey);
    this.ttl = ttl;
  }
}
