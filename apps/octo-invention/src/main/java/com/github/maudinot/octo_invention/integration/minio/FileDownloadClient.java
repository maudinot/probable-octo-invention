package com.github.maudinot.octo_invention.integration.minio;

import java.net.URI;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.github.maudinot.octo_invention.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
public class FileDownloadClient {

    @Value("${minio.client.url}") private String url;
    @Value("${minio.client.accessKey}") private String accessKey;
    @Value("${minio.client.secretKey}") private String secretKey;
    @Value("${minio.client.bucket}") private String bucket;

    public FileDownloadResult downloadFile(String fileUrl, String metadataType, String filename) {
        URI endpointUri = URI.create(url);
        String key = extractKey(fileUrl);

        log.debug("Downloading file from MinIO: bucket={}, key={}", bucket, key);

        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(endpointUri)
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(getObjectRequest);
            GetObjectResponse response = responseStream.response();

            // Read the response body
            byte[] data;

            // Prefer metadata's type (stored at upload time) over S3 response type.
            // S3/Minio may return application/octet-stream if content type wasn't set on upload.
            String contentType;
            if (metadataType != null && !metadataType.isEmpty()) {
                contentType = metadataType;
            } else {
                contentType = response.contentType();
            }

            if (contentType != null && !contentType.isEmpty()) {
                // Minio/S3 may return a bare type like "application" without a subtype.
                // MediaType.parseMediaType() is strict and requires the '/' separator.
                if (!contentType.contains("/")) {
                    contentType = contentType + "/octet-stream";
                }
                MediaType mediaType = MediaType.parseMediaType(contentType);
                data = readResponse(responseStream);
                log.info("File {} downloaded with content type {}", fileUrl, mediaType);
                return new FileDownloadResult(data, mediaType, filename);
            } else {
                data = readResponse(responseStream);
                log.info("File {} downloaded with default content type", fileUrl);
                return new FileDownloadResult(data, MediaType.APPLICATION_OCTET_STREAM, filename);
            }
        } catch (S3Exception e) {
            log.error("S3 error downloading file: {} - {}", e.statusCode(), e.getMessage());
            throw new ResourceNotFoundException(e.getMessage(), e.statusCode());
        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage());
            throw new ResourceNotFoundException(e.getMessage(), 500);
        }
    }

    private byte[] readResponse(InputStream stream) throws java.io.IOException {
        return stream.readAllBytes();
    }

    private String extractKey(String fileUrl) {
        URI uri = URI.create(fileUrl);
        String path = uri.getPath();

        // URL format: http://localhost:9000/bucket/key
        // e.g., http://localhost:9000/octo-invention-files/1.png
        String bucketPrefix = "/" + this.bucket + "/";
        if (path.startsWith(bucketPrefix)) {
            return path.substring(bucketPrefix.length());
        }

        // Fallback: just use the last path segment
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
