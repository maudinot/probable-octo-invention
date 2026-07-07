package com.github.maudinot.octo_invention.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "file_metadata")
@NoArgsConstructor
public class FileMetadata {

    public static enum Status { PENDING, PROCESSING, READY, FAILED }

    public FileMetadata(String name, Long size, String type, String url, String previewUrl,
            String uploadDate, String downloadDate, String operatorId, String status) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.url = url;
        this.previewUrl = previewUrl;
        this.uploadDate = uploadDate;
        this.downloadDate = downloadDate;
        this.operatorId = operatorId;
        this.status = status != null ? status : Status.PENDING.toString();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter private Long id;
    
    @Column(nullable = false)
    @Getter @Setter private String name;
    
    @Getter @Setter private Long size;
    
    @Column(nullable = false)
    @Getter @Setter private String type;
    
    @Column(nullable = false)
    @Getter @Setter private String url;
    
    @Getter @Setter private String previewUrl;
    
    @Column(nullable = false)
    @Getter @Setter private String uploadDate;
    
    @Getter @Setter private String downloadDate;
    
    @Column(length = 20)
    @Getter @Setter private String operatorId;
    
    @Column(nullable = false)
    @Getter @Setter private String status;
    
    @Column(length = 200)
    @Getter @Setter private String errorMessage;
}
