package com.evomanex.AppRH.infra.security.oauth;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.UserRepository;
import com.evomanex.AppRH.service.OAuthLoginCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthAuthenticationSuccessHandlerTest {

    private static final String FRONTEND_URL =
            "http://localhost:5173";

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuthLoginCodeService loginCodeService;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oauth2User;

    @Mock
    private User user;

    private OAuthAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler =
                new OAuthAuthenticationSuccessHandler(
                        userRepository,
                        loginCodeService,
                        FRONTEND_URL
                );
    }

    @Test
    void shouldCreateLoginCodeAndRedirectToFrontend() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                authentication.getPrincipal()
        ).thenReturn(
                oauth2User
        );

        doReturn(
                "user-uuid-123"
        ).when(
                oauth2User
        ).getAttribute(
                "apprhUserId"
        );

        when(
                userRepository.findById(
                        "user-uuid-123"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                loginCodeService.createCode(
                        user
                )
        ).thenReturn(
                "temporary-code"
        );

        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        assertThat(
                response.getStatus()
        ).isEqualTo(
                302
        );

        assertThat(
                response.getRedirectedUrl()
        ).isEqualTo(
                "http://localhost:5173/oauth/callback"
                        + "?code=temporary-code"
        );

        verify(
                userRepository
        ).findById(
                "user-uuid-123"
        );

        verify(
                loginCodeService
        ).createCode(
                user
        );
    }

    @Test
    void shouldConvertNumericUserIdToString() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                authentication.getPrincipal()
        ).thenReturn(
                oauth2User
        );

        doReturn(
                12345
        ).when(
                oauth2User
        ).getAttribute(
                "apprhUserId"
        );

        when(
                userRepository.findById(
                        "12345"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                loginCodeService.createCode(
                        user
                )
        ).thenReturn(
                "temporary-code"
        );

        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        verify(
                userRepository
        ).findById(
                "12345"
        );
    }

    @Test
    void shouldEncodeLoginCodeInRedirectUrl() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(
                authentication.getPrincipal()
        ).thenReturn(
                oauth2User
        );

        doReturn(
                "user-uuid-123"
        ).when(
                oauth2User
        ).getAttribute(
                "apprhUserId"
        );

        when(
                userRepository.findById(
                        "user-uuid-123"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                loginCodeService.createCode(
                        user
                )
        ).thenReturn(
                "code with spaces"
        );

        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        assertThat(
                response.getRedirectedUrl()
        ).contains(
                "code=code%20with%20spaces"
        );
    }

    @Test
    void shouldRejectOAuthPrincipalWithoutAppRhUserId() {
        when(
                authentication.getPrincipal()
        ).thenReturn(
                oauth2User
        );

        doReturn(
                null
        ).when(
                oauth2User
        ).getAttribute(
                "apprhUserId"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertThatThrownBy(
                () ->
                        handler.onAuthenticationSuccess(
                                request,
                                response,
                                authentication
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O usuário OAuth não possui "
                                + "identificador AppRH."
                );

        verify(
                userRepository,
                never()
        ).findById(
                org.mockito.ArgumentMatchers
                        .anyString()
        );
    }

    @Test
    void shouldRejectInvalidOAuthPrincipal() {
        when(
                authentication.getPrincipal()
        ).thenReturn(
                "invalid-principal"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertThatThrownBy(
                () ->
                        handler.onAuthenticationSuccess(
                                request,
                                response,
                                authentication
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Usuário OAuth inválido."
                );
    }

    @Test
    void shouldRejectUnknownAppRhUser() {
        when(
                authentication.getPrincipal()
        ).thenReturn(
                oauth2User
        );

        doReturn(
                "unknown-user-id"
        ).when(
                oauth2User
        ).getAttribute(
                "apprhUserId"
        );

        when(
                userRepository.findById(
                        "unknown-user-id"
                )
        ).thenReturn(
                Optional.empty()
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertThatThrownBy(
                () ->
                        handler.onAuthenticationSuccess(
                                request,
                                response,
                                authentication
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Usuário AppRH não encontrado."
                );

        verify(
                loginCodeService,
                never()
        ).createCode(
                org.mockito.ArgumentMatchers
                        .any(
                                User.class
                        )
        );
    }

    @Test
    void shouldRejectInactiveAppRhUser() {
        when(
                authentication.getPrincipal()
        ).thenReturn(
                oauth2User
        );

        doReturn(
                "user-uuid-123"
        ).when(
                oauth2User
        ).getAttribute(
                "apprhUserId"
        );

        when(
                userRepository.findById(
                        "user-uuid-123"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.isEnabled()
        ).thenReturn(
                false
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertThatThrownBy(
                () ->
                        handler.onAuthenticationSuccess(
                                request,
                                response,
                                authentication
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "A conta AppRH não está ativa."
                );

        verify(
                loginCodeService,
                never()
        ).createCode(
                user
        );
    }
}