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

import com.github.maudinot.octo_invention.domain.FileMetadata;
import com.github.maudinot.octo_invention.domain.RawFile;
import com.github.maudinot.octo_invention.domain.User;
import com.github.maudinot.octo_invention.integration.minio.FileDownloadResult;
import com.github.maudinot.octo_invention.repository.UserRepository;
import com.github.maudinot.octo_invention.service.AsyncFileUploadService;
import com.github.maudinot.octo_invention.service.FileDownloadService;
import com.github.maudinot.octo_invention.service.FileMetadataService;

import jakarta.servlet.http.HttpServletRequest;
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
        var m = fileMetadataService.getFileMetadata(id);
        return ResponseEntity.ok(m);
    }

    @GetMapping(value = "/files/{id}/download")
    public ResponseEntity<?> getFile(@PathVariable("id") Long id) {
        log.debug("Received request to download file {}", id);
        FileDownloadResult downloadedFile = fileDownloadService.downloadFile(id);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(downloadedFile.type());
        if (downloadedFile.filename() != null) {
            builder.header("Content-Disposition", "attachment; filename=\"" + downloadedFile.filename() + "\"");
        }
        return builder.body(downloadedFile.data());
    }

    @GetMapping(value = "/files", produces = "application/json")
    public ResponseEntity<Collection<FileMetadata>> getAllFiles() {
        var files = fileMetadataService.getAllFiles();
        return ResponseEntity.ok(files);
    }

}
