package org.atlas.common.framework.storage.model;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UploadFileRequest extends BaseRequest {

  private byte[] bytes;
  private String contentType;
  private Map<String, String> metadata;
}
