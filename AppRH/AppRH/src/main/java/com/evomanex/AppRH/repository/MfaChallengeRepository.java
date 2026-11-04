package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.domain.mfa.MfaChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface MfaChallengeRepository
        extends JpaRepository<
        MfaChallenge,
        Long
        > {

    Optional<MfaChallenge>
    findByChallengeHashAndConsumedAtIsNull(
            String challengeHash
    );

    long deleteByExpiresAtBefore(
            OffsetDateTime dateTime
    );
}