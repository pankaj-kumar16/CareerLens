package com.careerlens.backend.repository;

import com.careerlens.backend.model.OtpRecord;
import com.careerlens.backend.model.OtpType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpRecordRepository extends MongoRepository<OtpRecord, String> {
    Optional<OtpRecord> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, OtpType type);

    int countByEmailAndTypeAndCreatedAtAfterAndUsedFalse(String email, OtpType type, Instant after);
}

