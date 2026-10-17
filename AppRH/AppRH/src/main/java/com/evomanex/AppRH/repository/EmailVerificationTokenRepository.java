package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.model.EmailVerificationToken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<
        EmailVerificationToken,
        UUID
        > {

    Optional<EmailVerificationToken>
    findByTokenHash(
            String tokenHash
    );

    List<EmailVerificationToken>
    findAllByUserIdAndUsedAtIsNull(
            String userId
    );
}