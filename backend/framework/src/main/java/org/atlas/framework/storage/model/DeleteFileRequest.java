package org.atlas.framework.storage.model;

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
public class DeleteFileRequest extends BaseRequest {

  public DeleteFileRequest(String bucket, String objectKey) {
    super(bucket, objectKey);
  }
}
