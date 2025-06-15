package org.atlas.infrastructure.storage.filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.storage.StoragePort;
import org.atlas.framework.storage.model.BaseRequest;
import org.atlas.framework.storage.model.DeleteFileRequest;
import org.atlas.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.framework.storage.model.GetFileRequest;
import org.atlas.framework.storage.model.UploadFileRequest;
import org.atlas.infrastructure.storage.filesystem.config.FilesystemProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilesystemStorageAdapter implements StoragePort {

  private final FilesystemProps props;

  @Override
  public void uploadFile(UploadFileRequest request) throws IOException {
    Path filePath = toFilePath(request);

    // Ensure parent directories exist
    Files.createDirectories(filePath.getParent());

    // Write file content
    try (OutputStream outputStream = Files.newOutputStream(filePath)) {
      outputStream.write(request.getFileContent());
    }
    log.info("Uploaded file successfully: {}", filePath);
  }

  @Override
  public byte[] getFile(GetFileRequest request) throws IOException {
    Path filePath = toFilePath(request);
    checkFileExists(filePath);
    return Files.readAllBytes(filePath);
  }

  @Override
  public String getDownloadUrl(GetDownloadUrlRequest request) throws IOException {
    throw new UnsupportedEncodingException();
  }

  @Override
  public void deleteFile(DeleteFileRequest request) throws IOException {
    Path filePath = toFilePath(request);
    checkFileExists(filePath);
    Files.delete(filePath);
    log.info("Deleted file successfully: {}", filePath);
  }

  private Path toFilePath(BaseRequest request) {
    return Paths.get(props.getRoot(), request.getBucket(), request.getObjectKey());
  }

  private void checkFileExists(Path filePath) throws FileNotFoundException {
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException("File not found: " + filePath);
    }
  }
}
