package org.atlas.infrastructure.storage.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.storage.StoragePort;
import org.atlas.framework.storage.model.DeleteFileRequest;
import org.atlas.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.framework.storage.model.GetFileRequest;
import org.atlas.framework.storage.model.UploadFileRequest;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@RequiredArgsConstructor
public class S3StorageAdapter implements StoragePort {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Override
  public void uploadFile(UploadFileRequest request) throws IOException {
    if (shouldUseMultipartUpload(request)) {
      doGeneralUpload(request);
    } else {
      doMultipartUpload(request);
    }
  }

  @Override
  public byte[] getFile(GetFileRequest request) throws IOException {
    // Build GetObjectRequest
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(request.getBucket())
        .key(request.getObjectKey())
        .build();

    // Fetch the object from S3
    try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
      // Convert InputStream to byte array
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = s3Object.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }
      return baos.toByteArray();
    }
  }

  @Override
  public String getDownloadUrl(GetDownloadUrlRequest request) throws IOException {
    GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
        .getObjectRequest(b -> b.bucket(request.getBucket()).key(request.getObjectKey()))
        .signatureDuration(request.getTtl())
        .build();
    PresignedGetObjectRequest presignedGetObjectRequest =
        s3Presigner.presignGetObject(getObjectPresignRequest);
    return presignedGetObjectRequest.url().toString();
  }

  @Override
  public void deleteFile(DeleteFileRequest request) throws IOException {
    DeleteObjectRequest s3Request = DeleteObjectRequest.builder()
        .bucket(request.getBucket())
        .key(request.getObjectKey())
        .build();
    s3Client.deleteObject(s3Request);
  }

  private boolean shouldUseMultipartUpload(UploadFileRequest request) {
    return request.getFileContent().length / 1024 >= 100; // 100 MB
  }

  private void doGeneralUpload(UploadFileRequest request) {
    PutObjectRequest s3Request = PutObjectRequest.builder()
        .bucket(request.getBucket())
        .key(request.getObjectKey())
        .metadata(request.getMetadata())
        .build();
    s3Client.putObject(s3Request, RequestBody.fromBytes(request.getFileContent()));
  }

  private void doMultipartUpload(UploadFileRequest request) throws IOException {
    // Initiate the multipart upload
    CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
        .bucket(request.getBucket())
        .key(request.getObjectKey())
        .metadata(request.getMetadata())
        .build();
    CreateMultipartUploadResponse createMultipartUploadResponse = s3Client.createMultipartUpload(
        createMultipartUploadRequest);
    String uploadId = createMultipartUploadResponse.uploadId();

    int partNumber = 1;
    List<CompletedPart> completedParts = new ArrayList<>();
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024 * 5); // 5 MB byte buffer

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getFileContent())) {
      int bytesRead;
      while ((bytesRead = inputStream.read(byteBuffer.array())) != -1) {
        byteBuffer.limit(bytesRead);

        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
            .bucket(request.getBucket())
            .key(request.getObjectKey())
            .uploadId(uploadId)
            .partNumber(partNumber)
            .build();

        UploadPartResponse partResponse = s3Client.uploadPart(
            uploadPartRequest,
            RequestBody.fromByteBuffer(byteBuffer));

        CompletedPart part = CompletedPart.builder()
            .partNumber(partNumber)
            .eTag(partResponse.eTag())
            .build();
        completedParts.add(part);

        byteBuffer.clear();
        partNumber++;
      }
    }

    // Complete the multipart upload
    CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
        .parts(completedParts)
        .build();
    s3Client.completeMultipartUpload(b -> b
        .bucket(request.getBucket())
        .key(request.getObjectKey())
        .uploadId(uploadId)
        .multipartUpload(completedMultipartUpload));
  }
}
