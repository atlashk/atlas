package org.atlas.framework.storage.model;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class GetFileRequest extends BaseRequest {

  public GetFileRequest(String bucket, String objectKey) {
    super(bucket, objectKey);
  }
}
