package com.theone.tobackend.domain.file.dto;

import com.theone.tobackend.domain.file.File;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {
    private String externalId;
    private String originalName;
    private String storedName;
    private String filePath;
    private String mimeType;
    private Long fileSize;
    private String serviceName;
    private String serviceId;
    private String downloadUrl;

    public static FileDto from(File f) {
        return FileDto.builder()
                .externalId(f.getExternalId())
                .originalName(f.getOriginalName())
                .storedName(f.getStoredName())
                .filePath(f.getFilePath())
                .mimeType(f.getMimeType())
                .fileSize(f.getFileSize())
                .serviceName(f.getServiceName())
                .serviceId(f.getServiceId())
                .downloadUrl("/api/files/download/" + f.getExternalId())
                .build();
    }
}
