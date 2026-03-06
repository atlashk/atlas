package org.atlas.libs.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.storage.StorageService;
import org.atlas.libs.framework.storage.model.CheckFileExistsRequest;
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

  /**
   * Create a bucket if it does not exist. If the bucket already exists, do nothing.
   */
  @Override
  public void createBucket(String bucketName) throws IOException {
    try {
      BucketExistsArgs existsArgs = BucketExistsArgs.builder()
          .bucket(bucketName)
          .build();
      boolean exists = minioClient.bucketExists(existsArgs);
      if (exists) {
        log.info("Bucket '{}' already exists", bucketName);
        return;
      }
    } catch (Exception e) {
      throw new IOException(String.format("Failed to check if bucket '%s' exists", bucketName), e);
    }

    try {
      MakeBucketArgs args = MakeBucketArgs.builder()
          .bucket(bucketName)
          .build();
      minioClient.makeBucket(args);
      log.info("Created bucket: {}", bucketName);
    } catch (Exception e) {
      throw new IOException(String.format("Failed to create bucket '%s'", bucketName), e);
    }
  }

  @Override
  public void uploadFile(UploadFileRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    byte[] bytes = request.getBytes();

    try (InputStream stream = new ByteArrayInputStream(bytes)) {
      PutObjectArgs args = PutObjectArgs.builder()
          .bucket(bucket)
          .object(objectKey)
          .stream(stream, bytes.length, -1)
          .contentType(request.getContentType())
          .build();
      minioClient.putObject(args);
      log.info("Uploaded object: {}/{} ({} bytes)", bucket, objectKey, bytes.length);
    } catch (Exception e) {
      throw new IOException("Failed to upload object: " + bucket + "/" + objectKey, e);
    }
  }

  @Override
  public boolean checkFileExists(CheckFileExistsRequest request) {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    StatObjectArgs args = StatObjectArgs.builder()
        .bucket(bucket)
        .object(objectKey)
        .build();

    try {
      minioClient.statObject(args);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public byte[] getFileContent(GetFileRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    GetObjectArgs args = GetObjectArgs.builder()
        .bucket(bucket)
        .object(objectKey)
        .build();

    try (InputStream stream = minioClient.getObject(args)) {
      byte[] data = stream.readAllBytes();
      log.info("Fetched object: {}/{} ({} bytes)", bucket, objectKey, data.length);
      return data;
    } catch (Exception e) {
      throw new IOException(String.format("Failed to get object: %s/%s", bucket, objectKey), e);
    }
  }

  @Override
  public String getDownloadUrl(GetDownloadUrlRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    Duration ttl = request.getTtl();
    int expirySeconds = (int) Math.max(1, Math.min(ttl.toSeconds(), 7 * 24 * 3600));
    GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
        .bucket(bucket)
        .object(objectKey)
        .method(Method.GET)
        .expiry(expirySeconds)
        .build();

    try {
      String url = minioClient.getPresignedObjectUrl(args);
      log.info("Generated presigned URL for object: {}/{} (ttl={}s)",
          bucket, objectKey, expirySeconds);
      return url;
    } catch (Exception e) {
      throw new IOException(
          String.format("Failed to generate presigned URL for object: %s/%s", bucket, objectKey),
          e);
    }
  }

  @Override
  public void deleteFile(DeleteFileRequest request) throws IOException {
    String bucket = request.getBucket();
    String objectKey = request.getObjectKey();
    RemoveObjectArgs args = RemoveObjectArgs.builder()
        .bucket(bucket)
        .object(objectKey)
        .build();

    try {
      minioClient.removeObject(args);
      log.info("Deleted object: {}/{}", bucket, objectKey);
    } catch (Exception e) {
      throw new IOException(
          String.format("Failed to delete object: %s/%s", bucket, objectKey), e);
    }
  }
}
