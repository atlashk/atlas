package org.atlas.libs.framework.storage;

import java.io.IOException;
import org.atlas.libs.framework.storage.model.DeleteFileRequest;
import org.atlas.libs.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.libs.framework.storage.model.GetFileRequest;
import org.atlas.libs.framework.storage.model.UploadFileRequest;

public interface StorageService {

  void uploadFile(UploadFileRequest request) throws IOException;

  byte[] getFileContent(GetFileRequest request) throws IOException;

  String getDownloadUrl(GetDownloadUrlRequest request) throws IOException;

  void deleteFile(DeleteFileRequest request) throws IOException;
}
