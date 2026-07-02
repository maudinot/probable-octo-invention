package com.github.maudinot.octo_invention.repository;

import com.github.maudinot.octo_invention.domain.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    FileMetadata findByUrl(String url);
}
