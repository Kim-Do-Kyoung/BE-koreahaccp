package domain.file;

import domain.file.dto.FileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public List<FileDto> upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("serviceName") String serviceName,
            @RequestParam(value = "serviceId", required = false) String serviceId,
            @RequestParam(value = "fileType", required = false) String fileType
    ) {
        return fileService.storeFiles(files, serviceName, serviceId, fileType)
                .stream().map(FileDto::from).collect(Collectors.toList());
    }

    @GetMapping("/{serviceName}/{serviceId}")
    public List<FileDto> list(@PathVariable String serviceName, @PathVariable String serviceId) {
        return fileService.getFiles(serviceName, serviceId).stream().map(FileDto::from).collect(Collectors.toList());
    }

    @GetMapping("/download/{externalId}")
    public ResponseEntity<Resource> download(@PathVariable String externalId) {
        File f = fileService.getFileByExternalId(externalId);
        Resource resource = fileService.loadFileAsResource(externalId);
        String encoded = URLEncoder.encode(f.getOriginalName() == null ? f.getStoredName() : f.getOriginalName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.getMimeType() == null ? "application/octet-stream" : f.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<Void> delete(@PathVariable String externalId) {
        fileService.deleteFile(externalId);
        return ResponseEntity.noContent().build();
    }
}
