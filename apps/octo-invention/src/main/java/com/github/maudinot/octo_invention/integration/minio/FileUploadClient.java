package com.github.maudinot.octo_invention.integration.minio;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.maudinot.octo_invention.domain.RawFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileUploadClient {

    @Value("${minio.client.url}") private String url;
    @Value("${minio.client.accessKey}") private String accessKey;
    @Value("${minio.client.secretKey}") private String secretKey;
    @Value("${minio.client.bucket}") private String bucket;

    public FileUploadResult uploadFile(RawFile file, long id) {
        try {
            String filename = file.filename();
            String ext = getFileExtension(filename);
            String key = id + ext;

            URI endpointUri = URI.create(url);

            try (S3Client s3Client = S3Client.builder()
                    .endpointOverride(endpointUri)
                    .region(Region.US_EAST_1)
                    .forcePathStyle(true)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build()) {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

                s3Client.putObject(putRequest,
                    RequestBody.fromBytes(file.bytes()));

                log.info("File {} uploaded to s3://{}/{}", filename, bucket, key);
                return new FileUploadResult(true, null, url + "/" + bucket + "/" + key);
            }
        } catch (S3Exception e) {
            log.error("S3 error uploading file: {} - {}", e.statusCode(), e.getMessage());
            return new FileUploadResult(false, "S3 error: " + e.getMessage(), null);
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Failed to upload file to S3", e);
            return new FileUploadResult(false, "Failed to upload file: " + e.getMessage(), null);
        }
    }

    private String getFileExtension(String filename) {
        int idx = filename.lastIndexOf(".");
        return idx > 0 ? filename.substring(idx + 1) : "";
    }
}
