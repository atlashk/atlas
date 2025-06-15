package org.atlas.framework.storage;

import java.io.IOException;
import org.atlas.framework.storage.model.DeleteFileRequest;
import org.atlas.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.framework.storage.model.GetFileRequest;
import org.atlas.framework.storage.model.UploadFileRequest;

public interface StoragePort {

  void uploadFile(UploadFileRequest request) throws IOException;

  byte[] getFile(GetFileRequest request) throws IOException;

  String getDownloadUrl(GetDownloadUrlRequest request) throws IOException;

  void deleteFile(DeleteFileRequest request) throws IOException;
}
