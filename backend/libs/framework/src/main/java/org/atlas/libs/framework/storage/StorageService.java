package org.atlas.libs.framework.storage;

import java.io.IOException;
import org.atlas.libs.framework.storage.model.CheckFileExistsRequest;
import org.atlas.libs.framework.storage.model.DeleteFileRequest;
import org.atlas.libs.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.libs.framework.storage.model.GetFileRequest;
import org.atlas.libs.framework.storage.model.UploadFileRequest;

public interface StorageService {

  void createBucket(String bucketName) throws IOException;

  void uploadFile(UploadFileRequest request) throws IOException;

  boolean checkFileExists(CheckFileExistsRequest request);

  byte[] getFileContent(GetFileRequest request) throws IOException;

  String getDownloadUrl(GetDownloadUrlRequest request) throws IOException;

  void deleteFile(DeleteFileRequest request) throws IOException;
}
