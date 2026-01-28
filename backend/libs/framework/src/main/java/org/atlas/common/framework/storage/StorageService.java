package org.atlas.common.framework.storage;

import java.io.IOException;
import org.atlas.common.framework.storage.model.DeleteFileRequest;
import org.atlas.common.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.common.framework.storage.model.GetFileRequest;
import org.atlas.common.framework.storage.model.UploadFileRequest;

public interface StorageService {

  void uploadFile(UploadFileRequest request) throws IOException;

  byte[] getFile(GetFileRequest request) throws IOException;

  String getDownloadUrl(GetDownloadUrlRequest request) throws IOException;

  void deleteFile(DeleteFileRequest request) throws IOException;
}
