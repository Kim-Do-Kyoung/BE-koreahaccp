package com.theone.tobackend.domain.quote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByCategoryContainingAndNameContaining(String category, String name);
}
