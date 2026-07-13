package com.github.maudinot.octo_invention.integration.minio;

import org.springframework.http.MediaType;

public record  FileDownloadResult(byte[] data, MediaType type) {
    
}
