package com.github.maudinot.octo_invention.integration.minio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.github.maudinot.octo_invention.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileDownloadClient {

    @Value("${minio.client.url}") private String url;
    @Value("${minio.client.accessKey}") private String accessKey;
    @Value("${minio.client.secretKey}") private String secretKey;
    @Value("${minio.client.bucket}") private String bucket;

    public FileDownloadResult downloadFile(String filename) {
        try {
            String downloadEndpoint = url + "/" + bucket + "/" + filename;

            RestClient restClient = RestClient.create();
            ResponseEntity<String> answer = restClient.get()
                .uri(downloadEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + java.util.Base64.getEncoder().encodeToString((accessKey + ":" + secretKey).getBytes()))
                .retrieve()
                .toEntity(String.class);
            log.info("File {} downloaded with code {}", filename, answer.getStatusCode());
            return new FileDownloadResult(answer.getBody().getBytes(), answer.getHeaders().getContentType());
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.error("Invalid credentials for download to MinIO");
            throw new ResourceNotFoundException(e.getMessage(), e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            log.error("Server error: {} - {}", e.getStatusCode(), e.getMessage());
            throw new ResourceNotFoundException(e.getMessage(), e.getStatusCode().value());
        }
    }
}
