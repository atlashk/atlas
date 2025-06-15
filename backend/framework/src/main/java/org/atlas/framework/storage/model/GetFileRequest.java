package org.atlas.framework.storage.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GetFileRequest extends BaseRequest {

  public GetFileRequest(String bucket, String objectKey) {
    super(bucket, objectKey);
  }
}
