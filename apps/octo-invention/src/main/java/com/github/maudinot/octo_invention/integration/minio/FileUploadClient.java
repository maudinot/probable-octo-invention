package com.github.maudinot.octo_invention.integration.minio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileUploadClient {

    @Value("${minio.client.url}") private String url;
    @Value("${minio.client.accessKey}") private  String accessKey;
    @Value("${minio.client.secretKey}") private String secretKey;
    @Value("${minio.client.bucket}") private String bucket;

    public FileUploadResult uploadFile(com.github.maudinot.octo_invention.domain.RawFile file, long id) {
        try {
            String filename = file.filename();
            String ext = getFileExtension(filename);
            String uploadEndpoint = url + "/" + bucket + "/" + id + ext;

            RestClient restClient = RestClient.create();
            ResponseEntity<Void> answer = restClient.put()
                .uri(uploadEndpoint)
                .body(file.bytes())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + java.util.Base64.getEncoder().encodeToString((accessKey + ":" + secretKey).getBytes()))
                .retrieve()
                .toBodilessEntity();
            log.info("File {} uploaded with code {}", filename, answer.getStatusCode());
            return new FileUploadResult(true, null, uploadEndpoint);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.error("Invalid credentials for upload to MinIO");
            return new FileUploadResult(false, "Invalid credentials", null);
        } catch (HttpServerErrorException e) {
            log.error("Server error: {} - {}", e.getStatusCode(), e.getMessage());
            return new FileUploadResult(false, "Server error", null);
        }
    }

    private String getFileExtension(String filename) {
        int idx = filename.lastIndexOf(".");
        return idx > 0 ? filename.substring(idx + 1) : "";
    }
}
