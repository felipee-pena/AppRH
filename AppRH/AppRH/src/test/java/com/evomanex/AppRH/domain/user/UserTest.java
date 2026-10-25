package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Company;

import org.junit.jupiter.api.Test;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateCandidatePendingEmailConfirmation() {
        User user =
                new User(
                        "candidato@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        assertThat(
                user.getLogin()
        ).isEqualTo(
                "candidato@example.com"
        );

        assertThat(
                user.getPassword()
        ).isEqualTo(
                "$2a$10$encoded"
        );

        assertThat(
                user.getRole()
        ).isEqualTo(
                UserRole.USER
        );

        assertThat(
                user.getStatus()
        ).isEqualTo(
                UserStatus.PENDING_EMAIL_CONFIRMATION
        );

        assertThat(
                user.isEmailVerified()
        ).isFalse();

        assertThat(
                user.isEnabled()
        ).isFalse();

        assertThat(
                user.isAccountNonLocked()
        ).isTrue();

        assertThat(
                user.isAccountNonExpired()
        ).isTrue();

        assertThat(
                user.isCredentialsNonExpired()
        ).isTrue();
    }

    @Test
    void shouldActivateCandidateAfterEmailConfirmation() {
        User user =
                new User(
                        "candidato@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        user.confirmEmail();

        assertThat(
                user.getStatus()
        ).isEqualTo(
                UserStatus.ACTIVE
        );

        assertThat(
                user.isEmailVerified()
        ).isTrue();

        assertThat(
                user.getEmailVerifiedAt()
        ).isNotNull();

        assertThat(
                user.isEnabled()
        ).isTrue();

        assertThat(
                user.isPendingEmailConfirmation()
        ).isFalse();
    }

    @Test
    void shouldMoveRhUserToPendingApprovalAfterEmailConfirmation() {
        User user =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        user.confirmEmail();

        assertThat(
                user.getStatus()
        ).isEqualTo(
                UserStatus.PENDING_APPROVAL
        );

        assertThat(
                user.isEmailVerified()
        ).isTrue();

        assertThat(
                user.isEnabled()
        ).isFalse();
    }

    @Test
    void shouldAssociateCompany() {
        User user =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        Company company =
                new Company();

        company.setId(
                10L
        );

        company.setName(
                "Empresa Teste"
        );

        user.setCompany(
                company
        );

        assertThat(
                user.getCompany()
        ).isSameAs(
                company
        );
    }

    @Test
    void shouldReturnCandidateAuthority() {
        User user =
                new User(
                        "candidato@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        assertThat(
                authorities
        )
                .extracting(
                        GrantedAuthority::getAuthority
                )
                .containsExactly(
                        "ROLE_USER"
                );
    }

    @Test
    void shouldReturnRhAuthorities() {
        User user =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        assertThat(
                authorities
        )
                .extracting(
                        GrantedAuthority::getAuthority
                )
                .containsExactly(
                        "ROLE_USERRH",
                        "ROLE_USER"
                );
    }

    @Test
    void shouldReturnAdminAuthorities() {
        User user =
                new User(
                        "admin@example.com",
                        "$2a$10$encoded",
                        UserRole.ADMIN
                );

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        assertThat(
                authorities
        )
                .extracting(
                        GrantedAuthority::getAuthority
                )
                .containsExactly(
                        "ROLE_ADMIN",
                        "ROLE_USERRH",
                        "ROLE_USER"
                );
    }

    @Test
    void shouldReturnUsernameFromLogin() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        assertThat(
                user.getUsername()
        ).isEqualTo(
                "usuario@example.com"
        );
    }
}