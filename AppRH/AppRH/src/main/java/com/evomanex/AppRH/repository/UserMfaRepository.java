package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.domain.mfa.UserMfa;
import com.evomanex.AppRH.infra.security.mfa.MfaMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMfaRepository
        extends JpaRepository<UserMfa, Long> {

    Optional<UserMfa>
    findByUser_IdAndMethod(
            String userId,
            MfaMethod method
    );

    Optional<UserMfa>
    findByUser_IdAndEnabledTrue(
            String userId
    );

    boolean existsByUser_IdAndEnabledTrue(
            String userId
    );

    void deleteByUser_IdAndMethod(
            String userId,
            MfaMethod method
    );
}