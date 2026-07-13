package com.betvictor.repository.repository;

import com.betvictor.repository.data.TextProcessingResultEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextProcessingResultRepository extends JpaRepository<TextProcessingResultEntity, Long> {
    List<TextProcessingResultEntity> findAllByOrderByIdDesc(Limit limit);
}
