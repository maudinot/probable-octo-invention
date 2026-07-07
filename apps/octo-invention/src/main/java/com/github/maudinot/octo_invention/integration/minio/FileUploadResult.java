package com.github.maudinot.octo_invention.integration.minio;

public record FileUploadResult(boolean success, String errorMessage, String uploadUrl) {
}
