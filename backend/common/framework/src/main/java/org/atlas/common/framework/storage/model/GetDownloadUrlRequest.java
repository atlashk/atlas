package org.atlas.common.framework.storage.model;

import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class GetDownloadUrlRequest extends BaseRequest {

  private Duration ttl;
}
