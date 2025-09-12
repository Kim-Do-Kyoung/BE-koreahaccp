package domain.file;

import lombok.RequiredArgsConstructor;
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
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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

            try (InputStream is = multipart.getInputStream()) {
                Files.copy(is, targetLocation, StandardCopyOption.REPLACE_EXISTING);

                File entity = File.builder()
                        .externalId(UUID.randomUUID().toString())
                        .originalName(original)
                        .storedName(storedName)
                        .filePath(targetDir.toString())            // 실제 저장된 디렉토리(절대경로)
                        .mimeType(multipart.getContentType())
                        .fileSize(multipart.getSize())
                        .serviceName(serviceName)
                        .serviceId(serviceId)
                        .fileType(resolveFileType(fileTypeStr))   // fileType은 enum 기반으로 처리
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
}
