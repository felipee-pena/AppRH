package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.domain.mfa.UserMfaRecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMfaRecoveryCodeRepository
        extends JpaRepository<
        UserMfaRecoveryCode,
        Long
        > {

    Optional<UserMfaRecoveryCode>
    findByUser_IdAndCodeHashAndUsedAtIsNull(
            String userId,
            String codeHash
    );

    List<UserMfaRecoveryCode>
    findAllByUser_IdAndUsedAtIsNull(
            String userId
    );

    void deleteAllByUser_Id(
            String userId
    );
}