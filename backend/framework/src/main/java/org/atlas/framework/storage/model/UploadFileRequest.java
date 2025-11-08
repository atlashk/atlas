package org.atlas.framework.storage.model;

import java.util.Map;
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
public class UploadFileRequest extends BaseRequest {

  private byte[] fileContent;
  private String contentType;
  private Map<String, String> metadata;

  public UploadFileRequest(String bucket, String objectKey, byte[] fileContent) {
    super(bucket, objectKey);
    this.fileContent = fileContent;
  }
}
