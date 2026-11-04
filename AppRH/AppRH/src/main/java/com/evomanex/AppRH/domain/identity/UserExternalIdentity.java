package com.evomanex.AppRH.domain.identity;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.oauth.ExternalIdentityProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(
        name = "user_external_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_external_identity_provider_subject",
                        columnNames = {
                                "provider",
                                "provider_user_id"
                        }
                ),
                @UniqueConstraint(
                        name =
                                "uk_external_identity_user_provider",
                        columnNames = {
                                "user_id",
                                "provider"
                        }
                )
        }
)
public class UserExternalIdentity {

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
                        name =
                                "fk_external_identity_user"
                )
        )
        private User user;

        @Enumerated(
                EnumType.STRING
        )
        @Column(
                name = "provider",
                nullable = false,
                length = 30
        )
        private ExternalIdentityProvider provider;

        @Column(
                name = "provider_user_id",
                nullable = false,
                length = 255
        )
        private String providerUserId;

        @Column(
                name = "provider_email",
                length = 320
        )
        private String providerEmail;

        @Column(
                name = "provider_username",
                length = 255
        )
        private String providerUsername;

        @Column(
                name = "email_verified",
                nullable = false
        )
        private boolean emailVerified;

        @Column(
                name = "created_at",
                nullable = false,
                updatable = false
        )
        private OffsetDateTime createdAt;

        @Column(
                name = "updated_at",
                nullable = false
        )
        private OffsetDateTime updatedAt;

        @Column(
                name = "last_login_at"
        )
        private OffsetDateTime lastLoginAt;

        protected UserExternalIdentity() {
        }

        public UserExternalIdentity(
                User user,
                ExternalIdentityProvider provider,
                String providerUserId,
                String providerEmail,
                String providerUsername,
                boolean emailVerified
        ) {
                validateUser(
                        user
                );

                validateProvider(
                        provider
                );

                validateProviderUserId(
                        providerUserId
                );

                this.user =
                        user;

                this.provider =
                        provider;

                this.providerUserId =
                        providerUserId.trim();

                this.providerEmail =
                        normalizeEmail(
                                providerEmail
                        );

                this.providerUsername =
                        normalizeUsername(
                                providerUsername
                        );

                this.emailVerified =
                        emailVerified;
        }

        @PrePersist
        private void prePersist() {
                OffsetDateTime now =
                        OffsetDateTime.now();

                createdAt =
                        now;

                updatedAt =
                        now;

                lastLoginAt =
                        now;
        }

        @PreUpdate
        private void preUpdate() {
                updatedAt =
                        OffsetDateTime.now();
        }

        public void registerLogin(
                String providerEmail,
                String providerUsername,
                boolean emailVerified
        ) {
                this.providerEmail =
                        normalizeEmail(
                                providerEmail
                        );

                this.providerUsername =
                        normalizeUsername(
                                providerUsername
                        );

                this.emailVerified =
                        emailVerified;

                this.lastLoginAt =
                        OffsetDateTime.now();
        }

        private void validateUser(
                User user
        ) {
                if (user == null) {
                        throw new IllegalArgumentException(
                                "O usuário AppRH é obrigatório."
                        );
                }

                if (
                        user.getId() == null
                                || user.getId().isBlank()
                ) {
                        throw new IllegalArgumentException(
                                "O usuário AppRH não possui "
                                        + "identificador válido."
                        );
                }
        }

        private void validateProvider(
                ExternalIdentityProvider provider
        ) {
                if (provider == null) {
                        throw new IllegalArgumentException(
                                "O provedor OAuth é obrigatório."
                        );
                }
        }

        private void validateProviderUserId(
                String providerUserId
        ) {
                if (
                        providerUserId == null
                                || providerUserId.isBlank()
                ) {
                        throw new IllegalArgumentException(
                                "O identificador do usuário "
                                        + "no provedor é obrigatório."
                        );
                }
        }

        private String normalizeEmail(
                String email
        ) {
                if (email == null) {
                        return null;
                }

                String normalized =
                        email
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

                if (normalized.isBlank()) {
                        return null;
                }

                return normalized;
        }

        private String normalizeUsername(
                String username
        ) {
                if (username == null) {
                        return null;
                }

                String normalized =
                        username.trim();

                if (normalized.isBlank()) {
                        return null;
                }

                return normalized;
        }

        public Long getId() {
                return id;
        }

        public User getUser() {
                return user;
        }

        public ExternalIdentityProvider
        getProvider() {
                return provider;
        }

        public String getProviderUserId() {
                return providerUserId;
        }

        public String getProviderEmail() {
                return providerEmail;
        }

        public String getProviderUsername() {
                return providerUsername;
        }

        public boolean isEmailVerified() {
                return emailVerified;
        }

        public OffsetDateTime getCreatedAt() {
                return createdAt;
        }

        public OffsetDateTime getUpdatedAt() {
                return updatedAt;
        }

        public OffsetDateTime getLastLoginAt() {
                return lastLoginAt;
        }

        @Override
        public boolean equals(
                Object object
        ) {
                if (this == object) {
                        return true;
                }

                if (
                        object == null
                                || getClass()
                                != object.getClass()
                ) {
                        return false;
                }

                UserExternalIdentity that =
                        (UserExternalIdentity) object;

                return id != null
                        && Objects.equals(
                        id,
                        that.id
                );
        }

        @Override
        public int hashCode() {
                return getClass().hashCode();
        }
}