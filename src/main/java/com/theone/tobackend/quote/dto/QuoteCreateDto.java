package com.theone.tobackend.quote.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class QuoteCreateDto {
    private String category;
    private String detail;
    private String name;
    private String contact;
    private String email;
    private String budget;
    private String farmLocation;
    private String message;
    // 파일 업로드 지원
    private MultipartFile file;
}
