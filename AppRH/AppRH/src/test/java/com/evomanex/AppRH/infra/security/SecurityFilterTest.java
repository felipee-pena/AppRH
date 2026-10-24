package com.evomanex.AppRH.infra.security;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.repository.UserRepository;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterWhenAuthorizationHeaderIsMissing()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/jobs"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        securityFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(
                tokenService,
                never()
        ).validateToken(
                org.mockito.ArgumentMatchers.anyString()
        );

        verify(
                filterChain
        ).doFilter(
                request,
                response
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void shouldAuthenticateUserWhenBearerTokenIsValid()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/jobs"
                );

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        when(
                tokenService.validateToken(
                        "valid-token"
                )
        ).thenReturn(
                "usuario@example.com"
        );

        when(
                userRepository.findByLogin(
                        "usuario@example.com"
                )
        ).thenReturn(
                user
        );

        securityFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertThat(
                authentication
        ).isNotNull();

        assertThat(
                authentication.getPrincipal()
        ).isSameAs(
                user
        );

        assertThat(
                authentication.getAuthorities()
        ).extracting(
                "authority"
        ).contains(
                "ROLE_USER"
        );

        verify(
                filterChain
        ).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/jobs"
                );

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                tokenService.validateToken(
                        "invalid-token"
                )
        ).thenReturn(
                ""
        );

        securityFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();

        verify(
                userRepository,
                never()
        ).findByLogin(
                org.mockito.ArgumentMatchers.anyString()
        );

        verify(
                filterChain
        ).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldIgnorePublicLoginEndpoint()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/auth/login"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        securityFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(
                tokenService,
                never()
        ).validateToken(
                org.mockito.ArgumentMatchers.anyString()
        );

        verify(
                filterChain
        ).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldIgnoreOptionsRequest()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "OPTIONS",
                        "/api/jobs"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        securityFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(
                tokenService,
                never()
        ).validateToken(
                org.mockito.ArgumentMatchers.anyString()
        );

        verify(
                filterChain
        ).doFilter(
                request,
                response
        );
    }
}