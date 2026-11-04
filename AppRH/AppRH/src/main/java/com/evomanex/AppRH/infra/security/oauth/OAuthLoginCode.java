package com.evomanex.AppRH.infra.security.oauth;

import com.evomanex.AppRH.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "oauth_login_codes"
)
public class OAuthLoginCode {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

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
            name = "code_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String codeHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(
            name = "consumed_at"
    )
    private OffsetDateTime consumedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected OAuthLoginCode() {
    }

    public OAuthLoginCode(
            User user,
            String codeHash,
            OffsetDateTime expiresAt
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "O usuário é obrigatório."
            );
        }

        if (
                codeHash == null
                        || codeHash.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "O hash do código OAuth é obrigatório."
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "A data de expiração é obrigatória."
            );
        }

        this.user = user;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt =
                OffsetDateTime.now();
    }

    public boolean isAvailable(
            OffsetDateTime now
    ) {
        if (now == null) {
            throw new IllegalArgumentException(
                    "A data de validação é obrigatória."
            );
        }

        return consumedAt == null
                && expiresAt.isAfter(
                now
        );
    }

    public void consume() {
        if (consumedAt != null) {
            throw new IllegalStateException(
                    "O código OAuth já foi consumido."
            );
        }

        consumedAt =
                OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getConsumedAt() {
        return consumedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}