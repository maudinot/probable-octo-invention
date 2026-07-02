package com.github.maudinot.octo_invention.service;

import java.time.format.DateTimeFormatter;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.maudinot.octo_invention.domain.FileMetadata;
import com.github.maudinot.octo_invention.exception.FileValidationException;
import com.github.maudinot.octo_invention.repository.FileMetadataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    @Value("${file.storage.upload.maxSize:10485760}") private final long maxFileSize;

    public void uploadFile(MultipartFile file, String operatorName) {
            validateFile(file);
            String uploadDate = java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String url = "";
            String type = "";

            fileMetadataRepository.save(new FileMetadata(
                file.getOriginalFilename(),
                file.getSize(),
                type,
                url,
                null,
                uploadDate,
                null,
                operatorName
            ));
            log.info("File uploaded - {} MB by {}", (double) file.getSize() / 1024 / 1024, operatorName);
    }

    public FileMetadata getFileMetadata(Long id) {
        return fileMetadataRepository.getReferenceById(id);
    }

    public Collection<FileMetadata> getAllFiles() {
        return fileMetadataRepository.findAll();
    }

    

    private void validateFile(MultipartFile multipartFile){
        if (multipartFile == null) {
            throw new FileValidationException("File is null");
        }
        if (multipartFile.getSize() > maxFileSize) {
            throw new FileValidationException("File is too large - max file size exceeded. Upload a smaller file.");
        }
        
        if (!isImage(multipartFile.getOriginalFilename())) {
            throw new FileValidationException("File type is not valid - allowed types are: image/jpeg, image/png, image/gif, image/webp");
        }
    }

    private boolean isImage(String filename) {
        String[] extensions = {"jpg", "jpeg", "png", "gif", "webp"};
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return java.util.Arrays.stream(extensions).anyMatch(e -> e.equals(ext));
    }
}
