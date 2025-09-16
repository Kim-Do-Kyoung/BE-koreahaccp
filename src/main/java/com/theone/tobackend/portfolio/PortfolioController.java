package com.theone.tobackend.portfolio;

import com.theone.tobackend.domain.portfolio.Portfolio;
import com.theone.tobackend.domain.portfolio.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 1️⃣ 전체 조회
    @GetMapping
    public ResponseEntity<List<Portfolio>> getAllPortfolios() {
        return ResponseEntity.ok(portfolioService.getAllPortfolios());
    }

    // 2️⃣ 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable Long id) {
        return portfolioService.getPortfolio(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3️⃣ 등록
    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(
            @ModelAttribute Portfolio portfolio,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile[] extraFiles
    ) {
        Portfolio saved = portfolioService.savePortfolio(portfolio, thumbnail, extraFiles);
        return ResponseEntity.ok(saved);
    }

    // 4️⃣ 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(
            @PathVariable Long id,
            @ModelAttribute Portfolio portfolio,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile[] extraFiles
    ) {
        return portfolioService.updatePortfolio(id, portfolio, thumbnail, extraFiles)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 5️⃣ 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        boolean deleted = portfolioService.deletePortfolio(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
