package org.atlas.framework.storage.model;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DeleteFileRequest extends BaseRequest {

  public DeleteFileRequest(String bucket, String objectKey) {
    super(bucket, objectKey);
  }
}
