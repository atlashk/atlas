package org.atlas.libs.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.storage.StorageService;
import org.atlas.libs.framework.storage.model.DeleteFileRequest;
import org.atlas.libs.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.libs.framework.storage.model.GetFileRequest;
import org.atlas.libs.framework.storage.model.UploadFileRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "storage.minio")
public class MinioStorageService implements StorageService {

  private final MinioClient minioClient;

  @Override
  public void uploadFile(UploadFileRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    byte[] bytes = request.getBytes();

    try {
      boolean exists = minioClient.bucketExists(
          BucketExistsArgs.builder().bucket(bucket).build());
      if (!exists) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        log.info("Created bucket: {}", bucket);
      }

      try (InputStream stream = new ByteArrayInputStream(bytes)) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(stream, bytes.length, -1)
                .contentType(request.getContentType())
                .build());
      }
      log.info("Uploaded object to MinIO: {}/{} ({} bytes)", bucket, objectKey, bytes.length);
    } catch (Exception e) {
      throw new IOException("Failed to upload object to MinIO: " + bucket + "/" + objectKey, e);
    }
  }

  @Override
  public byte[] getFileContent(GetFileRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    try (InputStream stream = minioClient.getObject(
        GetObjectArgs.builder()
            .bucket(bucket)
            .object(objectKey)
            .build())) {
      byte[] data = stream.readAllBytes();
      log.info("Fetched object from MinIO: {}/{} ({} bytes)", bucket, objectKey, data.length);
      return data;
    } catch (Exception e) {
      throw new IOException("Failed to get object from MinIO: " + bucket + "/" + objectKey, e);
    }
  }

  @Override
  public String getDownloadUrl(GetDownloadUrlRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    Duration ttl = request.getTtl();

    int expirySeconds = (int) Math.max(1, Math.min(ttl.toSeconds(), 7 * 24 * 3600));
    try {
      String url = minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .bucket(bucket)
              .object(objectKey)
              .method(Method.GET)
              .expiry(expirySeconds)
              .build());
      log.info("Generated presigned URL for MinIO object: {}/{} (ttl={}s)", bucket, objectKey,
          expirySeconds);
      return url;
    } catch (Exception e) {
      throw new IOException(
          "Failed to generate presigned URL for MinIO object: " + bucket + "/" + objectKey, e);
    }
  }

  @Override
  public void deleteFile(DeleteFileRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
      log.info("Deleted object from MinIO: {}/{}", bucket, objectKey);
    } catch (Exception e) {
      throw new IOException("Failed to delete object from MinIO: " + bucket + "/" + objectKey, e);
    }
  }
}
