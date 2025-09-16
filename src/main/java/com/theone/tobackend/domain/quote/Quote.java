package com.theone.tobackend.domain.quote;

import com.theone.tobackend.domain.IdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "quote")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Quote extends IdEntity {

    @Column(nullable = false)
    private String category;

    @Column
    private String detail;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private String email;

    @Column
    private String budget;

    @Column
    private String farmLocation;

    @Column
    private String message;

    @Column
    private String fileName;

    // ✅ 상태 (예: 대기중, 처리중, 완료)
    @Column(nullable = false)
    @Builder.Default
    private String status = "대기중";

}
