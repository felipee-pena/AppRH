package com.evomanex.AppRH.domain.mfa;

import com.evomanex.AppRH.domain.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name =
                "mfa_challenges"
)
public class MfaChallenge {

    private static final int
            MAX_FAILED_ATTEMPTS =
            5;

    @Id
    @GeneratedValue(
            strategy =
                    GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(
            fetch =
                    FetchType.LAZY,
            optional =
                    false
    )
    @JoinColumn(
            name =
                    "user_id",
            nullable =
                    false
    )
    private User user;

    @Column(
            name =
                    "challenge_hash",
            nullable =
                    false,
            unique =
                    true,
            length =
                    64
    )
    private String challengeHash;

    @Column(
            name =
                    "expires_at",
            nullable =
                    false
    )
    private OffsetDateTime expiresAt;

    @Column(
            name =
                    "consumed_at"
    )
    private OffsetDateTime consumedAt;

    @Column(
            name =
                    "failed_attempts",
            nullable =
                    false
    )
    private int failedAttempts;

    @Column(
            name =
                    "created_at",
            nullable =
                    false,
            updatable =
                    false
    )
    private OffsetDateTime createdAt;

    protected MfaChallenge() {
    }

    public MfaChallenge(
            User user,
            String challengeHash,
            OffsetDateTime expiresAt
    ) {
        this.user =
                user;

        this.challengeHash =
                challengeHash;

        this.expiresAt =
                expiresAt;

        this.failedAttempts =
                0;
    }

    @PrePersist
    private void prePersist() {
        createdAt =
                OffsetDateTime.now();
    }

    public boolean isAvailable(
            OffsetDateTime now
    ) {
        return consumedAt == null
                && expiresAt.isAfter(
                now
        )
                && failedAttempts
                < MAX_FAILED_ATTEMPTS;
    }

    public void registerFailure() {
        failedAttempts++;

        if (
                failedAttempts
                        > MAX_FAILED_ATTEMPTS
        ) {
            failedAttempts =
                    MAX_FAILED_ATTEMPTS;
        }
    }

    public void consume() {
        consumedAt =
                OffsetDateTime.now();
    }

    public User getUser() {
        return user;
    }
}