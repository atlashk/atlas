package org.atlas.framework.storage.model;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

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
