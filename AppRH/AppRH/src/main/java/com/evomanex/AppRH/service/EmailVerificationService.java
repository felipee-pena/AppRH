package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.events.EmailVerificationRequestedEvent;
import com.evomanex.AppRH.model.EmailVerificationToken;
import com.evomanex.AppRH.repository.EmailVerificationTokenRepository;
import com.evomanex.AppRH.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.time.OffsetDateTime;

import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class EmailVerificationService {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmailVerificationService.class
            );

    private static final int TOKEN_BYTES =
            32;

    private static final long TOKEN_VALIDITY_HOURS =
            24L;

    private final UserRepository userRepository;

    private final EmailVerificationTokenRepository
            tokenRepository;

    private final ApplicationEventPublisher
            eventPublisher;

    private final SecureRandom secureRandom;

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationTokenRepository tokenRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository =
                userRepository;

        this.tokenRepository =
                tokenRepository;

        this.eventPublisher =
                eventPublisher;

        this.secureRandom =
                new SecureRandom();
    }

    @Transactional
    public void createVerificationToken(
            User user
    ) {
        if (
                !user.isPendingEmailConfirmation()
        ) {
            return;
        }

        invalidatePreviousTokens(
                user.getId()
        );

        String rawToken =
                generateRawToken();

        String tokenHash =
                hashToken(
                        rawToken
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        tokenHash,
                        OffsetDateTime
                                .now()
                                .plusHours(
                                        TOKEN_VALIDITY_HOURS
                                )
                );
        tokenRepository.save(
                token
        );

        LOGGER.info(
                "Token de confirmação criado para {}",
                user.getLogin()
        );

        eventPublisher.publishEvent(
                new EmailVerificationRequestedEvent(
                        user.getLogin(),
                        rawToken
                )
        );

        LOGGER.info(
                "Evento de verificação publicado para {}",
                user.getLogin()
        );

        tokenRepository.save(
                token
        );

        eventPublisher.publishEvent(
                new EmailVerificationRequestedEvent(
                        user.getLogin(),
                        rawToken
                )
        );
    }

    @Transactional
    public void confirmEmail(
            String rawToken
    ) {
        String normalizedToken =
                rawToken.trim();

        String tokenHash =
                hashToken(
                        normalizedToken
                );

        EmailVerificationToken token =
                tokenRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Token de confirmação inválido"
                                        )
                        );

        if (token.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token de confirmação já utilizado"
            );
        }

        if (token.isExpired()) {
            token.invalidate();

            tokenRepository.save(
                    token
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token de confirmação expirado"
            );
        }

        User user =
                token.getUser();

        if (user.isEmailVerified()) {
            token.markAsUsed();

            tokenRepository.save(
                    token
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O e-mail já foi confirmado"
            );
        }

        user.confirmEmail();
        token.markAsUsed();

        userRepository.save(
                user
        );

        tokenRepository.save(
                token
        );
    }

    @Transactional
    public void resendVerification(
            String login
    ) {
        String normalizedLogin =
                login
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        userRepository
                .findByLoginIgnoreCase(
                        normalizedLogin
                )
                .filter(
                        User::isPendingEmailConfirmation
                )
                .ifPresent(
                        this::createVerificationToken
                );
    }

    private void invalidatePreviousTokens(
            String userId
    ) {
        List<EmailVerificationToken> activeTokens =
                tokenRepository
                        .findAllByUserIdAndUsedAtIsNull(
                                userId
                        );

        if (activeTokens.isEmpty()) {
            return;
        }

        activeTokens.forEach(
                EmailVerificationToken::invalidate
        );

        tokenRepository.saveAll(
                activeTokens
        );
    }

    private String generateRawToken() {
        byte[] bytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(
                bytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        bytes
                );
    }

    private String hashToken(
            String rawToken
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(
                            hash
                    );
        }
        catch (
                NoSuchAlgorithmException exception
        ) {
            throw new IllegalStateException(
                    "SHA-256 não está disponível",
                    exception
            );
        }
    }
}