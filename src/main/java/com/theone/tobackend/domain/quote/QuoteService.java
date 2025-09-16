package com.theone.tobackend.domain.quote;

import com.theone.tobackend.domain.file.FileService;
import com.theone.tobackend.quote.dto.QuoteCreateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuoteService {
    private final QuoteRepository quoteRepository;
    private final FileService fileService;

    @Transactional
    public Quote saveQuote(QuoteCreateDto request) {
        // DTO -> 엔티티 변환 (Builder 사용)
        Quote quote = Quote.builder()
                .category(request.getCategory())
                .detail(request.getDetail())
                .name(request.getName())
                .contact(request.getContact())
                .email(request.getEmail())
                .budget(request.getBudget())
                .farmLocation(request.getFarmLocation())
                .message(request.getMessage())
                .build();

        Quote savedQuote = quoteRepository.save(quote);

        // 파일 처리 (파일이 있으면 단일 파일도 배열로 감싸서 전달)
        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            fileService.storeFiles(
                    new MultipartFile[]{ file },
                    "quote",
                    savedQuote.getId().toString(),
                    null // fileTypeStr, 필요하면 지정
            );
        }

        return savedQuote;
    }

    @Transactional
    public Optional<Quote> updateQuote(Long id, QuoteCreateDto request) {
        return quoteRepository.findById(id).map(quote -> {
            quote.setCategory(request.getCategory());
            quote.setDetail(request.getDetail());
            quote.setName(request.getName());
            quote.setContact(request.getContact());
            quote.setEmail(request.getEmail());
            quote.setBudget(request.getBudget());
            quote.setFarmLocation(request.getFarmLocation());
            quote.setMessage(request.getMessage());

            // 파일 갱신 처리
            MultipartFile file = request.getFile();
            if (file != null && !file.isEmpty()) {
                fileService.storeFiles(
                        new MultipartFile[]{ file },
                        "quote",
                        quote.getId().toString(),
                        null
                );
            }

            return quote;
        });
    }

    public List<Quote> searchQuotes(String category, String name) {
        return quoteRepository.findByCategoryContainingAndNameContaining(
                category != null ? category : "",
                name != null ? name : ""
        );
    }

    // 전체 조회
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    // 단일 조회
    public Optional<Quote> getQuote(Long id) {
        return quoteRepository.findById(id);
    }
}