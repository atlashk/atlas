package org.atlas.framework.storage.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class GetFileRequest extends BaseRequest {

  public GetFileRequest(String bucket, String objectKey) {
    super(bucket, objectKey);
  }
}
