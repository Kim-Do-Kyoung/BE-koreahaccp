package com.theone.tobackend.domain.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    // 생성자에서 업로드 디렉토리 준비
    public FileService(FileRepository fileRepository,
                           @Value("${file.upload-dir}") String uploadDir) {
        this.fileRepository = fileRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory.", ex);
        }
    }

    public File storeFile(MultipartFile file, String serviceName, String serviceId, String fileTypeStr) {
        return storeFiles(new MultipartFile[]{ file }, serviceName, serviceId, fileTypeStr).get(0);
    }

    public List<File> storeFiles(MultipartFile[] files, String serviceName, String serviceId, String fileTypeStr) {
        List<File> saved = new ArrayList<>();
        if (files == null || files.length == 0) {
            return saved;
        }

        // path은 serviceName 기준으로 관리
        String safeService = serviceName == null || serviceName.isBlank() ? "default" : serviceName;
        Path targetDir = this.fileStorageLocation.resolve(safeService).normalize();

        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory for service: " + safeService, e);
        }

        for (MultipartFile multipart : files) {
            String original = StringUtils.cleanPath(multipart.getOriginalFilename() == null ? "" : multipart.getOriginalFilename());
            if (original.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + original);
            }

            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx > -1) ext = original.substring(idx); // includes '.'

            String storedName = UUID.randomUUID().toString() + ext;
            Path targetLocation = targetDir.resolve(storedName);

// 파일 저장
            try (InputStream is = multipart.getInputStream()) {
                Files.copy(is, targetLocation, StandardCopyOption.REPLACE_EXISTING);

                // 여기서부터 절대경로 대신 URL 형태로 저장
                String fileUrl = "/uploads/" + safeService + "/" + storedName;

                File entity = File.builder()
                        .externalId(UUID.randomUUID().toString())
                        .originalName(original)
                        .storedName(storedName)
                        .filePath(fileUrl)   // ✅ URL 경로만 저장
                        .mimeType(multipart.getContentType())
                        .fileSize(multipart.getSize())
                        .serviceName(serviceName)
                        .serviceId(serviceId)
                        .fileType(resolveFileType(fileTypeStr))
                        .build();

                saved.add(fileRepository.save(entity));
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file " + original, e);
            }
        }
        return saved;
    }

    private FileType resolveFileType(String s) {
        if (s == null || s.isBlank()) {
            try { return FileType.ATTACHMENT; } catch (Exception ex) { return null; }
        }
        try { return FileType.valueOf(s); }
        catch (Exception ex) {
            try { return FileType.ATTACHMENT; } catch (Exception e) { return null; }
        }
    }

    
    @Transactional(readOnly = true)
    public List<File> getFiles(String serviceName, String serviceId) {
        return fileRepository.findByServiceNameAndServiceId(serviceName, serviceId);
    }

    
    @Transactional(readOnly = true)
    public File getFileByExternalId(String externalId) {
        return fileRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("File not found: " + externalId));
    }

    
    @Transactional(readOnly = true)
    public Resource loadFileAsResource(String externalId) {
        File file = getFileByExternalId(externalId);
        Path filePath = Paths.get(file.getFilePath()).resolve(file.getStoredName()).normalize();
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + externalId);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + externalId, e);
        }
    }

    
    public void deleteFile(String externalId) {
        File file = getFileByExternalId(externalId);
        Path path = Paths.get(file.getFilePath()).resolve(file.getStoredName());
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 삭제 실패도 DB 삭제 전 로그로 남기고 진행하거나 rollback 원하면 예외 던지기
        }
        fileRepository.delete(file);
    }

    public List<File> getFilesByServiceName(String serviceName) {
        return fileRepository.findAllByServiceName(serviceName);
    }

    @Transactional(readOnly = true)
    public String getStoredFilePath(String serviceName, String serviceId, int index) {
        List<File> files = getFiles(serviceName, serviceId);
        if (files.isEmpty() || index >= files.size()) return null;

        File file = files.get(index);
        // 실제 접근 가능한 URL을 리턴하고 싶으면 도메인이나 contextPath 붙이기 가능
        return Paths.get(file.getFilePath()).resolve(file.getStoredName()).toString();
    }
}
