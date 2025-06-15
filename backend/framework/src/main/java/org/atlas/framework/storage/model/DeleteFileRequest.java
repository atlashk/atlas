package org.atlas.framework.storage.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeleteFileRequest extends BaseRequest {

  public DeleteFileRequest(String bucket, String objectKey) {
    super(bucket, objectKey);
  }
}
