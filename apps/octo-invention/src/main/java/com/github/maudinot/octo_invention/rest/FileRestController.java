package com.github.maudinot.octo_invention.rest;

import java.util.Base64;
import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.github.maudinot.octo_invention.domain.FileMetadata;
import com.github.maudinot.octo_invention.domain.User;
import com.github.maudinot.octo_invention.integration.minio.FileDownloadResult;
import com.github.maudinot.octo_invention.repository.UserRepository;
import com.github.maudinot.octo_invention.service.AsyncFileUploadService;
import com.github.maudinot.octo_invention.service.FileDownloadService;
import com.github.maudinot.octo_invention.service.FileMetadataService;
import com.github.maudinot.octo_invention.domain.RawFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class FileRestController {

    private final FileMetadataService fileMetadataService;

    private final AsyncFileUploadService asyncFileUploadService;

    private final FileDownloadService fileDownloadService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public FileRestController(FileMetadataService fileMetadataService,
                              AsyncFileUploadService asyncFileUploadService,
                              FileDownloadService fileDownloadService,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.fileMetadataService = fileMetadataService;
        this.asyncFileUploadService = asyncFileUploadService;
        this.fileDownloadService = fileDownloadService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(value = "/files", produces = "application/json")
    public ResponseEntity<?> uploadFile(HttpServletRequest request) {
        // Extract Basic Auth header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String encoded = authHeader.substring(6);
        String decoded = new String(Base64.getDecoder().decode(encoded));
        int colonIdx = decoded.indexOf(':');
        if (colonIdx <= 0 || colonIdx >= decoded.length() - 1) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials format");
        }

        String username = decoded.substring(0, colonIdx);
        String password = decoded.substring(colonIdx + 1);

        // Look up user in repository
        var userOpt = userRepository.findByName(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found: " + username);
        }

        User user = userOpt.get();
        // Plain-text comparison (passwords stored as-is for debugging)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password for user: " + username);
        }

        log.info("User {} authenticated, uploading file", username);

        // Read raw body as bytes and wrap as RawFile
        try {
            byte[] fileBytes = request.getInputStream().readAllBytes();
            String contentType = request.getContentType();
            String filename = request.getHeader("X-FileName");
            if (filename == null || filename.isEmpty()) {
                filename = "upload";
            }
            RawFile file = new RawFile(fileBytes, filename, contentType, fileBytes.length);

            FileMetadata uploadedFileMetadata = fileMetadataService.uploadFile(file, user.getName());
            asyncFileUploadService.uploadFile(file, uploadedFileMetadata);
            return ResponseEntity.accepted().body(uploadedFileMetadata);
        } catch (java.io.IOException e) {
            log.error("Failed to read request body", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file");
        }
    }

    @GetMapping(value = "/files/{id}", produces = "application/json")
    public ResponseEntity<?> getFileMetadata(@PathVariable("id") Long id) {
        try {
            var m = fileMetadataService.getFileMetadata(id);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/files/{id}/download")
    public ResponseEntity<?> getFile(@PathVariable("id") Long id) {
        try {
            FileDownloadResult downloadedFile = fileDownloadService.downloadFile(id);
            return ResponseEntity.ok().header("Content-Type", downloadedFile.type().getType()).body(downloadedFile.data());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/files", produces = "application/json")
    public ResponseEntity<Collection<FileMetadata>> getAllFiles() {
        var files = fileMetadataService.getAllFiles();
        return ResponseEntity.ok(files);
    }

}
