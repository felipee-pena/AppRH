package com.evomanex.AppRH.domain.mfa;

import com.evomanex.AppRH.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_mfa_recovery_codes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mfa_recovery_code_hash",
                        columnNames = {
                                "code_hash"
                        }
                )
        }
)
public class UserMfaRecoveryCode {

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
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_mfa_recovery_code_user"
            )
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
            name = "used_at"
    )
    private OffsetDateTime usedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected UserMfaRecoveryCode() {
    }

    public UserMfaRecoveryCode(
            User user,
            String codeHash
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
                    "O hash do código de recuperação "
                            + "é obrigatório."
            );
        }

        this.user =
                user;

        this.codeHash =
                codeHash;
    }

    @PrePersist
    private void prePersist() {
        createdAt =
                OffsetDateTime.now();
    }

    public boolean isAvailable() {
        return usedAt == null;
    }

    public void consume() {
        if (usedAt != null) {
            throw new IllegalStateException(
                    "O código de recuperação "
                            + "já foi utilizado."
            );
        }

        usedAt =
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

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}