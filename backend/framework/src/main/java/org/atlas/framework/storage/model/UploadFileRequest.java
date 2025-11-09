package org.atlas.framework.storage.model;

import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class UploadFileRequest extends BaseRequest {

  private byte[] fileContent;
  private String contentType;
  private Map<String, String> metadata;

  public UploadFileRequest(String bucket, String objectKey, byte[] fileContent) {
    super(bucket, objectKey);
    this.fileContent = fileContent;
  }

  public UploadFileRequest(String bucket, String objectKey, byte[] fileContent, String contentType) {
    super(bucket, objectKey);
    this.fileContent = fileContent;
    this.contentType = contentType;
  }

  public UploadFileRequest(String bucket, String objectKey, byte[] fileContent, String contentType,
      Map<String, String> metadata) {
    super(bucket, objectKey);
    this.fileContent = fileContent;
    this.contentType = contentType;
    this.metadata = metadata;
  }
}
