package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService service;

    @Test
    void shouldLoadUserUsingNormalizedLogin() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        when(
                userRepository.findByLogin(
                        "usuario@example.com"
                )
        ).thenReturn(
                user
        );

        UserDetails result =
                service.loadUserByUsername(
                        "  Usuario@Example.COM  "
                );

        assertThat(
                result
        ).isSameAs(
                user
        );

        assertThat(
                result.getUsername()
        ).isEqualTo(
                "usuario@example.com"
        );

        verify(
                userRepository
        ).findByLogin(
                "usuario@example.com"
        );
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(
                userRepository.findByLogin(
                        "inexistente@example.com"
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(
                () ->
                        service.loadUserByUsername(
                                "  INEXISTENTE@EXAMPLE.COM "
                        )
        )
                .isInstanceOf(
                        UsernameNotFoundException.class
                )
                .hasMessage(
                        "Usuário não encontrado"
                );

        verify(
                userRepository
        ).findByLogin(
                "inexistente@example.com"
        );
    }

    @Test
    void shouldPreserveAlreadyNormalizedLogin() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        when(
                userRepository.findByLogin(
                        "usuario@example.com"
                )
        ).thenReturn(
                user
        );

        UserDetails result =
                service.loadUserByUsername(
                        "usuario@example.com"
                );

        assertThat(
                result
        ).isSameAs(
                user
        );
    }
}