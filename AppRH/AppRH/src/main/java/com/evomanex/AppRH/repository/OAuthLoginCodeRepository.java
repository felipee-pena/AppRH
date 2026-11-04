package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.infra.security.oauth.OAuthLoginCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthLoginCodeRepository
        extends JpaRepository<OAuthLoginCode, Long> {

    Optional<OAuthLoginCode>
    findByCodeHashAndConsumedAtIsNull(
            String codeHash
    );
}