package com.theone.tobackend.quote.controller;

import com.theone.tobackend.domain.quote.Quote;
import com.theone.tobackend.domain.quote.QuoteService;
import com.theone.tobackend.quote.dto.QuoteCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quote")
@RequiredArgsConstructor
public class QuoteController {
    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<Quote> createQuote(@ModelAttribute QuoteCreateDto request) {
        // DTO 그대로 서비스에 전달
        Quote savedQuote = quoteService.saveQuote(request);
        return ResponseEntity.ok(savedQuote);
    }

   @GetMapping
    public ResponseEntity<List<Quote>> getAllQuotes() {
        return ResponseEntity.ok(quoteService.getAllQuotes());
   }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> getQuote(@PathVariable Long id) {
        return quoteService.getQuote(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quote> updateQuote(
            @PathVariable Long id,
            @ModelAttribute QuoteCreateDto request) {

        return quoteService.updateQuote(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/search")
    public ResponseEntity<List<Quote>> searchQuotes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(quoteService.searchQuotes(category, name));
    }
}
