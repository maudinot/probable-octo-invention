package com.github.maudinot.octo_invention.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.github.maudinot.octo_invention.domain.FileMetadata;
import com.github.maudinot.octo_invention.exception.FileValidationException;
import com.github.maudinot.octo_invention.repository.FileMetadataRepository;
import com.github.maudinot.octo_invention.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FileMetadataServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private FileMetadataService fileMetadataService;

    @BeforeEach
    void setUp() {
        reset(fileMetadataRepository, userRepository);
        fileMetadataService = new FileMetadataService(fileMetadataRepository, 100);
    }

    @Test
    void testUploadFile_ShouldSaveMetadata() {
        // Given
        String operatorName = "testuser";
        MultipartFile multipartFile = new MockMultipartFile("test", "test.png", "image/png", new byte[10]);

        // When
        fileMetadataService.uploadFile(multipartFile, operatorName);

        // Then
        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
    }

    @Test
    void testUploadFile_ThrowExceptionIfFileSizeExceedsLimit() {
        // Given
        String operatorName = "testuser";
        byte[] largeImage = new byte[101];
        MultipartFile multipartFile = new MockMultipartFile("test", "test.png", "image/png", largeImage);
        // Then
        assertThrows(FileValidationException.class, () -> {
            fileMetadataService.uploadFile(multipartFile, operatorName);
        });
    }

    @Test
    void testUploadFile_ThrowExceptionIfNotSupportedImageType() {
        // Given
        String operatorName = "testuser";
        MultipartFile multipartFile = new MockMultipartFile("test", "test.txt", "text/plain", new byte[10]);
        // Then
        assertThrows(FileValidationException.class, () -> {
            fileMetadataService.uploadFile(multipartFile, operatorName);
        });
    }

    @Test
    void testUploadFile_ThrowsExceptionIfFileIsNull() {
        // Given
        String operatorName = "testuser";
        assertThrows(FileValidationException.class, () -> {
            fileMetadataService.uploadFile(null, operatorName);
        });
    }

    @Test
    void testGetAllFiles_ShouldReturnAllFiles() {
        // Given
        Collection<FileMetadata> files = new ArrayList<>();
        doReturn(files).when(fileMetadataRepository).findAll();

        // When
        Collection<FileMetadata> result = fileMetadataService.getAllFiles();

        // Then
        assertIterableEquals(files, result);
    }

    @Test
    void testGetAllFiles_ShouldReturnEmptyList() {
        doReturn(Collections.emptyList()).when(fileMetadataRepository).findAll();
        assertNotNull(fileMetadataService.getAllFiles());
    }

    @Test
    void testGetAllFiles_ShouldReturnNull() {
        doReturn((Collection<FileMetadata>) null).when(fileMetadataRepository).findAll();
        assertNull(fileMetadataService.getAllFiles());
    }

}
