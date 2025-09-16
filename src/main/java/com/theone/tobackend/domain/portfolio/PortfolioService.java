package com.theone.tobackend.domain.portfolio;

import com.theone.tobackend.domain.file.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final FileService fileService;

    // 포트폴리오 등록
    @Transactional
    public Portfolio savePortfolio(Portfolio portfolio,
                                   MultipartFile thumbnail,
                                   MultipartFile[] extraFiles) {
        Portfolio saved = portfolioRepository.save(portfolio);

        // 썸네일 저장
        if (thumbnail != null && !thumbnail.isEmpty()) {
            fileService.storeFiles(
                    new MultipartFile[]{thumbnail},
                    "portfolioThumbnail",
                    saved.getId().toString(),
                    null
            );
            // 저장 경로 가져와서 엔티티에 세팅
            saved.setThumbnailPath(fileService.getStoredFilePath("portfolioThumbnail", saved.getId().toString(), 0));
        }

        // 추가 이미지 저장
        if (extraFiles != null && extraFiles.length > 0) {
            fileService.storeFiles(extraFiles, "portfolioExtra", saved.getId().toString(), null);
        }

        return saved;
    }

    // 전체 조회
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }

    // 단일 조회
    public Optional<Portfolio> getPortfolio(Long id) {
        return portfolioRepository.findById(id);
    }

    // 삭제 (파일 삭제 호출 제거)
    @Transactional
    public boolean deletePortfolio(Long id) {
        return portfolioRepository.findById(id).map(p -> {
            portfolioRepository.delete(p);
            return true;
        }).orElse(false);
    }

    // 업데이트
    @Transactional
    public Optional<Portfolio> updatePortfolio(Long id,
                                               Portfolio updated,
                                               MultipartFile thumbnail,
                                               MultipartFile[] extraFiles) {
        return portfolioRepository.findById(id).map(existing -> {
            existing.setProjectName(updated.getProjectName());
            existing.setCategory(updated.getCategory());
            existing.setDescription(updated.getDescription());
            existing.setArea(updated.getArea());
            existing.setCost(updated.getCost());
            existing.setCompletedDate(updated.getCompletedDate());

            // 썸네일 교체
            if (thumbnail != null && !thumbnail.isEmpty()) {
                fileService.storeFiles(
                        new MultipartFile[]{thumbnail},
                        "portfolioThumbnail",
                        existing.getId().toString(),
                        null
                );
                existing.setThumbnailPath(fileService.getStoredFilePath("portfolioThumbnail", existing.getId().toString(), 0));
            }

            // 추가 이미지 교체
            if (extraFiles != null && extraFiles.length > 0) {
                fileService.storeFiles(extraFiles, "portfolioExtra", existing.getId().toString(), null);
            }

            return existing;
        });
    }
}
