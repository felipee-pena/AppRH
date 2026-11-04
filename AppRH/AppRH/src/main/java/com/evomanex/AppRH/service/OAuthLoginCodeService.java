package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.oauth.OAuthLoginCode;
import com.evomanex.AppRH.repository.OAuthLoginCodeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
public class OAuthLoginCodeService {

    private static final int CODE_BYTE_LENGTH =
            32;

    private static final long EXPIRATION_MINUTES =
            2L;

    private final OAuthLoginCodeRepository repository;

    private final HashService hashService;

    private final SecureRandom secureRandom;

    public OAuthLoginCodeService(
            OAuthLoginCodeRepository repository,
            HashService hashService
    ) {
        this.repository = repository;
        this.hashService = hashService;
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public String createCode(
            User user
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "O usuário é obrigatório."
            );
        }

        byte[] randomBytes =
                new byte[CODE_BYTE_LENGTH];

        secureRandom.nextBytes(
                randomBytes
        );

        String rawCode =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                randomBytes
                        );

        String codeHash =
                hashService.sha256(
                        rawCode
                );

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        codeHash,
                        OffsetDateTime.now()
                                .plusMinutes(
                                        EXPIRATION_MINUTES
                                )
                );

        repository.save(
                loginCode
        );

        return rawCode;
    }

    @Transactional
    public User consumeCode(
            String rawCode
    ) {
        if (
                rawCode == null
                        || rawCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "O código OAuth é obrigatório."
            );
        }

        String codeHash =
                hashService.sha256(
                        rawCode
                );

        OAuthLoginCode loginCode =
                repository
                        .findByCodeHashAndConsumedAtIsNull(
                                codeHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Código OAuth inválido."
                                        )
                        );

        if (
                !loginCode.isAvailable(
                        OffsetDateTime.now()
                )
        ) {
            throw new IllegalArgumentException(
                    "Código OAuth expirado ou já consumido."
            );
        }

        loginCode.consume();

        repository.save(
                loginCode
        );

        return loginCode.getUser();
    }
}