    package domain.file;

    import jakarta.persistence.*;
    import kr.cs.gz.domain.IdEntity;
    import lombok.*;
    import org.hibernate.annotations.UuidGenerator;

    @Entity
    @Table(name = "files")
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public class File extends IdEntity {
        @UuidGenerator
        @Column(nullable = false, unique = true, length = 36)
        private String externalId;

        /**
         * 사용자가 업로드한 원래 파일명
         */
        @Column(nullable = false)
        private String originalName;

        /**
         * 서버에 저장된 실제 파일명 (UUID + 확장자 포함)
         */
        @Column(nullable = false)
        private String storedName;

        /**
         * 저장된 전체 경로
         */
        private String filePath;

        /**
         * 파일의 MIME 타입 (예: image/png, application/pdf 등)
         */
        private String mimeType;

        /**
         * 파일 크기 (바이트 단위)
         */
        private Long fileSize;

        /**
         * 연동된 도메인 이름 (예: board, inquiry 등)
         */
        private String serviceName;

        /**
         * 해당 도메인 객체의 ID
         */
        private String serviceId;

        /**
         * 파일의 역할: paid_content, attachment 등
         */
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private FileType fileType;

    }
