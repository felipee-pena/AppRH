package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Company;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void shouldCreateUserWithPendingEmailConfirmationStatus() {
        User user = new User(
                "usuario@teste.com",
                "senha-criptografada",
                UserRole.USER
        );

        assertEquals(
                "usuario@teste.com",
                user.getLogin()
        );

        assertEquals(
                "usuario@teste.com",
                user.getUsername()
        );

        assertEquals(
                "senha-criptografada",
                user.getPassword()
        );

        assertEquals(
                UserRole.USER,
                user.getRole()
        );

        assertEquals(
                UserStatus.PENDING_EMAIL_CONFIRMATION,
                user.getStatus()
        );

        assertNull(
                user.getId()
        );

        assertNull(
                user.getCompany()
        );

        assertNull(
                user.getEmailVerifiedAt()
        );

        assertFalse(
                user.isEmailVerified()
        );

        assertTrue(
                user.isPendingEmailConfirmation()
        );
    }

    @Test
    void shouldInstantiateUserUsingJpaConstructor() throws Exception {
        Constructor<User> constructor =
                User.class.getDeclaredConstructor();

        constructor.setAccessible(
                true
        );

        User user =
                assertDoesNotThrow(
                        () -> {
                            return constructor.newInstance();
                        }
                );

        assertNotNull(
                user
        );

        assertNull(
                user.getId()
        );

        assertNull(
                user.getLogin()
        );

        assertNull(
                user.getPassword()
        );

        assertNull(
                user.getRole()
        );

        assertNull(
                user.getCompany()
        );

        assertNull(
                user.getStatus()
        );

        assertNull(
                user.getEmailVerifiedAt()
        );
    }

    @Test
    void shouldReturnUserAuthorityForRegularUser() {
        User user = new User(
                "usuario@teste.com",
                "senha",
                UserRole.USER
        );

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        assertEquals(
                List.of(
                        "ROLE_USER"
                ),
                authorities
                        .stream()
                        .map(
                                GrantedAuthority::getAuthority
                        )
                        .toList()
        );
    }

    @Test
    void shouldReturnUserRhAndUserAuthoritiesForUserRh() {
        User user = new User(
                "rh@teste.com",
                "senha",
                UserRole.USERRH
        );

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        assertEquals(
                List.of(
                        "ROLE_USERRH",
                        "ROLE_USER"
                ),
                authorities
                        .stream()
                        .map(
                                GrantedAuthority::getAuthority
                        )
                        .toList()
        );
    }

    @Test
    void shouldReturnAllAuthoritiesForAdmin() {
        User user = new User(
                "admin@teste.com",
                "senha",
                UserRole.ADMIN
        );

        Collection<? extends GrantedAuthority> authorities =
                user.getAuthorities();

        assertEquals(
                List.of(
                        "ROLE_ADMIN",
                        "ROLE_USERRH",
                        "ROLE_USER"
                ),
                authorities
                        .stream()
                        .map(
                                GrantedAuthority::getAuthority
                        )
                        .toList()
        );
    }

    @Test
    void shouldKeepRegularUserAccountUnlockedBeforeEmailConfirmation() {
        User user = new User(
                "usuario@teste.com",
                "senha",
                UserRole.USER
        );

        assertTrue(
                user.isAccountNonExpired()
        );

        assertTrue(
                user.isAccountNonLocked()
        );

        assertTrue(
                user.isCredentialsNonExpired()
        );

        assertFalse(
                user.isEnabled()
        );
    }

    @Test
    void shouldActivateRegularUserAfterEmailConfirmation() {
        User user = new User(
                "usuario@teste.com",
                "senha",
                UserRole.USER
        );

        user.confirmEmail();

        assertEquals(
                UserStatus.ACTIVE,
                user.getStatus()
        );

        assertNotNull(
                user.getEmailVerifiedAt()
        );

        assertTrue(
                user.isEmailVerified()
        );

        assertFalse(
                user.isPendingEmailConfirmation()
        );

        assertTrue(
                user.isEnabled()
        );

        assertTrue(
                user.isAccountNonLocked()
        );
    }

    @Test
    void shouldKeepUserRhPendingApprovalAfterEmailConfirmation() {
        User user = new User(
                "rh@teste.com",
                "senha",
                UserRole.USERRH
        );

        user.confirmEmail();

        assertEquals(
                UserStatus.PENDING_APPROVAL,
                user.getStatus()
        );

        assertNotNull(
                user.getEmailVerifiedAt()
        );

        assertTrue(
                user.isEmailVerified()
        );

        assertFalse(
                user.isPendingEmailConfirmation()
        );

        assertFalse(
                user.isEnabled()
        );

        assertTrue(
                user.isAccountNonLocked()
        );
    }

    @Test
    void shouldAssociateCompanyWithUser() {
        User user = new User(
                "rh@teste.com",
                "senha",
                UserRole.USERRH
        );

        Company company =
                instantiateCompany();

        user.setCompany(
                company
        );

        assertSame(
                company,
                user.getCompany()
        );
    }

    @Test
    void shouldReturnTrueWhenComparingSameInstance() {
        User user = new User(
                "usuario@teste.com",
                "senha",
                UserRole.USER
        );

        assertEquals(
                user,
                user
        );
    }

    @Test
    void shouldReturnFalseWhenComparingWithNull() {
        User user = new User(
                "usuario@teste.com",
                "senha",
                UserRole.USER
        );

        assertNotEquals(
                user,
                null
        );
    }

    @Test
    void shouldReturnFalseWhenComparingWithDifferentType() {
        User user = new User(
                "usuario@teste.com",
                "senha",
                UserRole.USER
        );

        assertNotEquals(
                user,
                "usuario"
        );
    }

    @Test
    void shouldReturnFalseWhenUsersHaveNoPersistedId() {
        User firstUser = new User(
                "primeiro@teste.com",
                "senha",
                UserRole.USER
        );

        User secondUser = new User(
                "segundo@teste.com",
                "senha",
                UserRole.USER
        );

        assertNotEquals(
                firstUser,
                secondUser
        );
    }

    @Test
    void shouldReturnStableHashCode() {
        User user = new User(
                "usuario@teste.com",
                "senha",
                UserRole.USER
        );

        assertEquals(
                User.class.hashCode(),
                user.hashCode()
        );
    }

    private Company instantiateCompany() {
        try {
            Constructor<Company> constructor =
                    Company.class.getDeclaredConstructor();

            constructor.setAccessible(
                    true
            );

            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Não foi possível instanciar Company para o teste",
                    exception
            );
        }
    }
}