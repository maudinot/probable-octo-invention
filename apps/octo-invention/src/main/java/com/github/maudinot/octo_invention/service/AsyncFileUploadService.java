package com.github.maudinot.octo_invention.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.maudinot.octo_invention.domain.FileMetadata;
import com.github.maudinot.octo_invention.exception.FileValidationException;
import com.github.maudinot.octo_invention.integration.minio.FileUploadClient;
import com.github.maudinot.octo_invention.integration.minio.FileUploadResult;
import com.github.maudinot.octo_invention.repository.FileMetadataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncFileUploadService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileUploadClient fileUploadClient;

    @Async
    public void uploadFile(MultipartFile file, FileMetadata metadata) throws FileValidationException {
        long fileId = metadata.getId();
        log.info("Starting async upload for file ID: {}", fileId);
        FileUploadResult result = fileUploadClient.uploadFile(file, fileId);
        
        if (result.success()) {
            metadata.setStatus(FileMetadata.Status.READY.toString());
            metadata.setUrl(result.uploadUrl());
            metadata.setErrorMessage(null);
            metadata = fileMetadataRepository.save(metadata);
            log.info("File upload completed: {} -> {}", metadata.getId(), result.uploadUrl());
        } else {
            metadata.setStatus(FileMetadata.Status.FAILED.toString());
            metadata.setUrl("");
            metadata.setErrorMessage(result.errorMessage());
            metadata = fileMetadataRepository.save(metadata);
            log.error("File upload FAILED: {} - {}", metadata.getId(), result.errorMessage());
        }
    }
}
