package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.domain.user.UserStatus;

import com.evomanex.AppRH.events.EmailVerificationRequestedEvent;

import com.evomanex.AppRH.model.EmailVerificationToken;

import com.evomanex.AppRH.repository.EmailVerificationTokenRepository;
import com.evomanex.AppRH.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.time.OffsetDateTime;

import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmailVerificationService service;

    @Test
    void shouldCreateHashedTokenAndPublishSingleEvent() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        when(
                tokenRepository.findAllByUserIdAndUsedAtIsNull(
                        user.getId()
                )
        ).thenReturn(
                List.of()
        );

        service.createVerificationToken(
                user
        );

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        EmailVerificationToken.class
                );

        ArgumentCaptor<EmailVerificationRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        EmailVerificationRequestedEvent.class
                );

        verify(
                tokenRepository,
                times(1)
        ).save(
                tokenCaptor.capture()
        );

        verify(
                eventPublisher,
                times(1)
        ).publishEvent(
                eventCaptor.capture()
        );

        EmailVerificationToken savedToken =
                tokenCaptor.getValue();

        EmailVerificationRequestedEvent publishedEvent =
                eventCaptor.getValue();

        assertThat(
                savedToken.getTokenHash()
        ).hasSize(
                64
        );

        assertThat(
                publishedEvent.destinationEmail()
        ).isEqualTo(
                "usuario@example.com"
        );

        assertThat(
                publishedEvent.rawToken()
        ).isNotBlank();

        assertThat(
                savedToken.getTokenHash()
        ).isEqualTo(
                sha256(
                        publishedEvent.rawToken()
                )
        );

        assertThat(
                savedToken.getTokenHash()
        ).doesNotContain(
                publishedEvent.rawToken()
        );

        assertThat(
                savedToken.getExpiresAt()
        ).isAfter(
                OffsetDateTime.now()
        );
    }

    @Test
    void shouldNotCreateTokenForConfirmedUser() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        user.confirmEmail();

        service.createVerificationToken(
                user
        );

        verify(
                tokenRepository,
                never()
        ).save(
                any(EmailVerificationToken.class)
        );

        verify(
                eventPublisher,
                never()
        ).publishEvent(
                any()
        );
    }

    @Test
    void shouldInvalidatePreviousActiveTokens() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken previousToken =
                new EmailVerificationToken(
                        user,
                        "a".repeat(64),
                        OffsetDateTime
                                .now()
                                .plusHours(10)
                );

        when(
                tokenRepository.findAllByUserIdAndUsedAtIsNull(
                        user.getId()
                )
        ).thenReturn(
                List.of(
                        previousToken
                )
        );

        service.createVerificationToken(
                user
        );

        assertThat(
                previousToken.isUsed()
        ).isTrue();

        verify(
                tokenRepository
        ).saveAll(
                List.of(
                        previousToken
                )
        );
    }

    @Test
    void shouldRejectInvalidToken() {
        when(
                tokenRepository.findByTokenHash(
                        sha256(
                                "token-invalido"
                        )
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.confirmEmail(
                                "token-invalido"
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .satisfies(
                        exception -> {
                            ResponseStatusException responseException =
                                    (ResponseStatusException) exception;

                            assertThat(
                                    responseException.getStatusCode()
                            ).isEqualTo(
                                    HttpStatus.BAD_REQUEST
                            );

                            assertThat(
                                    responseException.getReason()
                            ).isEqualTo(
                                    "Token de confirmação inválido"
                            );
                        }
                );
    }

    @Test
    void shouldRejectExpiredTokenAndInvalidateIt() {
        String rawToken =
                "token-expirado";

        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        sha256(
                                rawToken
                        ),
                        OffsetDateTime
                                .now()
                                .minusMinutes(1)
                );

        when(
                tokenRepository.findByTokenHash(
                        sha256(
                                rawToken
                        )
                )
        ).thenReturn(
                Optional.of(
                        token
                )
        );

        assertThatThrownBy(
                () ->
                        service.confirmEmail(
                                rawToken
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "Token de confirmação expirado"
                );

        assertThat(
                token.isUsed()
        ).isTrue();

        verify(
                tokenRepository
        ).save(
                token
        );
    }

    @Test
    void shouldConfirmCandidateAndMarkTokenAsUsed() {
        String rawToken =
                "token-valido";

        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        sha256(
                                rawToken
                        ),
                        OffsetDateTime
                                .now()
                                .plusHours(1)
                );

        when(
                tokenRepository.findByTokenHash(
                        sha256(
                                rawToken
                        )
                )
        ).thenReturn(
                Optional.of(
                        token
                )
        );

        service.confirmEmail(
                rawToken
        );

        assertThat(
                user.isEmailVerified()
        ).isTrue();

        assertThat(
                user.getStatus()
        ).isEqualTo(
                UserStatus.ACTIVE
        );

        assertThat(
                token.isUsed()
        ).isTrue();

        verify(
                userRepository
        ).save(
                user
        );

        verify(
                tokenRepository
        ).save(
                token
        );
    }

    @Test
    void shouldMoveRhUserToPendingApproval() {
        String rawToken =
                "token-rh-valido";

        User user =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        sha256(
                                rawToken
                        ),
                        OffsetDateTime
                                .now()
                                .plusHours(1)
                );

        when(
                tokenRepository.findByTokenHash(
                        sha256(
                                rawToken
                        )
                )
        ).thenReturn(
                Optional.of(
                        token
                )
        );

        service.confirmEmail(
                rawToken
        );

        assertThat(
                user.getStatus()
        ).isEqualTo(
                UserStatus.PENDING_APPROVAL
        );

        assertThat(
                user.isEmailVerified()
        ).isTrue();
    }

    @Test
    void shouldResendOnlyForPendingUser() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        when(
                userRepository.findByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                tokenRepository.findAllByUserIdAndUsedAtIsNull(
                        user.getId()
                )
        ).thenReturn(
                List.of()
        );

        service.resendVerification(
                "  Usuario@Example.COM  "
        );

        verify(
                tokenRepository
        ).save(
                any(EmailVerificationToken.class)
        );

        verify(
                eventPublisher
        ).publishEvent(
                any(EmailVerificationRequestedEvent.class)
        );
    }

    private static String sha256(
            String value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return HexFormat
                    .of()
                    .formatHex(
                            digest.digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );
        }
        catch (Exception exception) {
            throw new IllegalStateException(
                    exception
            );
        }
    }
}