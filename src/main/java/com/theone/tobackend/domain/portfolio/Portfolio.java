package com.theone.tobackend.domain.portfolio;

import com.theone.tobackend.domain.IdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio extends IdEntity {

    // 프로젝트명
    private String projectName;

    // 카테고리
    private String category;

    // 프로젝트 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 면적 (㎡)
    private Integer area;

    // 시공금액 (만원)
    private Integer cost;

    // 완료 날짜
    private LocalDate completedDate;

    // 썸네일 파일 경로 (단일 이미지)
    private String thumbnailPath;
}
