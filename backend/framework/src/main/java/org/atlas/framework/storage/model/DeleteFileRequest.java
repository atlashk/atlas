package org.atlas.framework.storage.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class DeleteFileRequest extends BaseRequest {

  public DeleteFileRequest(String bucket, String objectKey) {
    super(bucket, objectKey);
  }
}
