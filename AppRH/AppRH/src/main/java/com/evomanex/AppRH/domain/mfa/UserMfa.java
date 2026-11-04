package com.evomanex.AppRH.domain.mfa;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.mfa.MfaMethod;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_mfa",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_user_mfa_user_method",
                        columnNames = {
                                "user_id",
                                "method"
                        }
                )
        }
)
public class UserMfa {

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

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name =
                    "method",
            nullable =
                    false,
            length =
                    30
    )
    private MfaMethod method;

    @Column(
            name =
                    "secret_encrypted",
            nullable =
                    false,
            columnDefinition =
                    "TEXT"
    )
    private String secretEncrypted;

    @Column(
            name =
                    "enabled",
            nullable =
                    false
    )
    private boolean enabled;

    @Column(
            name =
                    "confirmed_at"
    )
    private OffsetDateTime confirmedAt;

    @Column(
            name =
                    "created_at",
            nullable =
                    false,
            updatable =
                    false
    )
    private OffsetDateTime createdAt;

    @Column(
            name =
                    "updated_at",
            nullable =
                    false
    )
    private OffsetDateTime updatedAt;

    @Column(
            name =
                    "last_used_at"
    )
    private OffsetDateTime lastUsedAt;

    protected UserMfa() {
    }

    public UserMfa(
            User user,
            MfaMethod method,
            String secretEncrypted
    ) {
        this.user =
                user;

        this.method =
                method;

        this.secretEncrypted =
                secretEncrypted;

        this.enabled =
                false;
    }

    @PrePersist
    private void prePersist() {
        OffsetDateTime now =
                OffsetDateTime.now();

        createdAt =
                now;

        updatedAt =
                now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt =
                OffsetDateTime.now();
    }

    public void confirm() {
        enabled =
                true;

        confirmedAt =
                OffsetDateTime.now();
    }

    public void disable() {
        enabled =
                false;

        confirmedAt =
                null;
    }

    public void registerUse() {
        lastUsedAt =
                OffsetDateTime.now();
    }

    public String getSecretEncrypted() {
        return secretEncrypted;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public User getUser() {
        return user;
    }
}