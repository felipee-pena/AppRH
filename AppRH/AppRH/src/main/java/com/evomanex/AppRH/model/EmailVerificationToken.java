package com.evomanex.AppRH.model;

import com.evomanex.AppRH.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "email_verification_tokens",
        schema = "public"
)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(
            name = "used_at"
    )
    private OffsetDateTime usedAt;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected EmailVerificationToken() {
    }

    public EmailVerificationToken(
            User user,
            String tokenHash,
            OffsetDateTime expiresAt
    ) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return OffsetDateTime
                .now()
                .isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markAsUsed() {
        this.usedAt =
                OffsetDateTime.now();
    }

    public void invalidate() {
        if (usedAt == null) {
            usedAt =
                    OffsetDateTime.now();
        }
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }
    public UUID getId() {
        return id;
    }
}