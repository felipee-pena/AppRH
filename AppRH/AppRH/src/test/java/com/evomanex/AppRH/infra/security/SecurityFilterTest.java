package com.evomanex.AppRH.infra.security;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.UserRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
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
    private User user;

    private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        securityFilter =
                new SecurityFilter(
                        tokenService,
                        userRepository
                );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWhenBearerTokenIsValid()
            throws ServletException, IOException {
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

        MockFilterChain filterChain =
                new MockFilterChain();

        List<GrantedAuthority> authorities =
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_USER"
                        )
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

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        doReturn(
                authorities
        ).when(
                user
        ).getAuthorities();

        securityFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNotNull();

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal()
        ).isSameAs(
                user
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .isAuthenticated()
        ).isTrue();

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getAuthorities()
        )
                .extracting(
                        GrantedAuthority::getAuthority
                )
                .containsExactly(
                        "ROLE_USER"
                );

        verify(
                tokenService
        ).validateToken(
                "valid-token"
        );

        verify(
                userRepository
        ).findByLogin(
                "usuario@example.com"
        );
    }

    @Test
    void shouldNotAuthenticateDisabledUser()
            throws ServletException, IOException {
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

        MockFilterChain filterChain =
                new MockFilterChain();

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

        when(
                user.isEnabled()
        ).thenReturn(
                false
        );

        securityFilter.doFilterInternal(
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
                tokenService
        ).validateToken(
                "valid-token"
        );

        verify(
                userRepository
        ).findByLogin(
                "usuario@example.com"
        );
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsAbsent()
            throws ServletException, IOException {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/jobs"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        securityFilter.doFilterInternal(
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
                tokenService,
                never()
        ).validateToken(
                anyString()
        );

        verify(
                userRepository,
                never()
        ).findByLogin(
                anyString()
        );
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/jobs"
                );

        request.addHeader(
                "Authorization",
                "Basic credentials"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        securityFilter.doFilterInternal(
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
                tokenService,
                never()
        ).validateToken(
                anyString()
        );

        verify(
                userRepository,
                never()
        ).findByLogin(
                anyString()
        );
    }

    @Test
    void shouldNotAuthenticateWhenTokenValidationReturnsBlankLogin()
            throws ServletException, IOException {
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

        MockFilterChain filterChain =
                new MockFilterChain();

        when(
                tokenService.validateToken(
                        "invalid-token"
                )
        ).thenReturn(
                ""
        );

        securityFilter.doFilterInternal(
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
                anyString()
        );
    }

    @Test
    void shouldNotAuthenticateWhenRepositoryDoesNotFindUser()
            throws ServletException, IOException {
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

        MockFilterChain filterChain =
                new MockFilterChain();

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
                null
        );

        securityFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void shouldPreserveExistingAuthentication()
            throws ServletException, IOException {
        UserDetails existingPrincipal =
                org.springframework.security.core.userdetails.User
                        .withUsername(
                                "existing@example.com"
                        )
                        .password(
                                "password"
                        )
                        .authorities(
                                "ROLE_USER"
                        )
                        .build();

        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        existingPrincipal,
                        null,
                        existingPrincipal.getAuthorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        existingAuthentication
                );

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

        MockFilterChain filterChain =
                new MockFilterChain();

        when(
                tokenService.validateToken(
                        "valid-token"
                )
        ).thenReturn(
                "usuario@example.com"
        );

        securityFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isSameAs(
                existingAuthentication
        );

        verify(
                userRepository,
                never()
        ).findByLogin(
                anyString()
        );
    }

    @Test
    void shouldSkipOptionsRequest() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "OPTIONS",
                        "/api/jobs"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipLoginEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/auth/login"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipRegistrationEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/auth/register"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipEmailConfirmationEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/auth/email/confirm"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipEmailResendEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/auth/email/resend"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipOAuthExchangeEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/auth/oauth/exchange"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipMfaVerificationEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/auth/mfa/verify"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipOAuthAuthorizationEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/oauth2/authorization/google"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipOAuthCallbackEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/login/oauth2/code/google"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipPublicCompaniesEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/companies"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipSwaggerEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/swagger-ui/index.html"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldSkipHealthEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/actuator/health"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isTrue();
    }

    @Test
    void shouldFilterProtectedJobsEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/jobs"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isFalse();
    }

    @Test
    void shouldFilterProtectedMfaSetupEndpoint() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/account/mfa/totp/setup"
                );

        assertThat(
                securityFilter.shouldNotFilter(
                        request
                )
        ).isFalse();
    }
}