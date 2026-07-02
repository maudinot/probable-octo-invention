package com.github.maudinot.octo_invention.rest;

import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.maudinot.octo_invention.domain.FileMetadata;
import com.github.maudinot.octo_invention.domain.User;
import com.github.maudinot.octo_invention.service.FileMetadataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileRestController {

    private final FileMetadataService fileMetadataService;

    @PostMapping(value = "/files", produces = "application/json")
    public ResponseEntity<?> uploadFile(@RequestParam User user, @RequestBody  MultipartFile file) {
        log.info("User {} tried to upload", user.getName());
        fileMetadataService.uploadFile(file, user.getName());
        return ResponseEntity.accepted().build();
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

    @GetMapping(value = "/files", produces = "application/json")
    public ResponseEntity<Collection<FileMetadata>> getAllFiles() {
        var files = fileMetadataService.getAllFiles();
        return ResponseEntity.ok(files);
    }

}
