package com.github.maudinot.octo_invention.service;

import org.springframework.stereotype.Service;

import com.github.maudinot.octo_invention.domain.FileMetadata;
import com.github.maudinot.octo_invention.exception.ResourceNotFoundException;
import com.github.maudinot.octo_invention.integration.minio.FileDownloadClient;
import com.github.maudinot.octo_invention.integration.minio.FileDownloadResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileMetadataService fileMetadataService;

    private final FileDownloadClient fileDownloadClient;

     public FileDownloadResult downloadFile(long id) {
            FileMetadata metadata = fileMetadataService.getFileMetadata(id);

            if(!"READY".equals(metadata.getStatus())) {
                throw new ResourceNotFoundException("File not ready for download");
            }

            return fileDownloadClient.downloadFile(metadata.getName());
        }
}
