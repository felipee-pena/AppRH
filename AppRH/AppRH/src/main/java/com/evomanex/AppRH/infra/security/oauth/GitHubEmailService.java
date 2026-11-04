package com.evomanex.AppRH.infra.security.oauth;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Locale;

@Service
public class GitHubEmailService {

    private static final String EMAILS_URL =
            "https://api.github.com/user/emails";

    private final RestClient restClient;

    public GitHubEmailService() {
        this(
                RestClient.create()
        );
    }

    GitHubEmailService(
            RestClient restClient
    ) {
        if (restClient == null) {
            throw new IllegalArgumentException(
                    "O cliente HTTP é obrigatório."
            );
        }

        this.restClient =
                restClient;
    }

    public String findPrimaryVerifiedEmail(
            String accessToken
    ) {
        if (
                accessToken == null
                        || accessToken.isBlank()
        ) {
            throw authenticationException(
                    "missing_github_access_token",
                    "O token de acesso do GitHub é obrigatório."
            );
        }

        List<GitHubEmailResponse> emails;

        try {
            emails =
                    restClient
                            .get()
                            .uri(
                                    EMAILS_URL
                            )
                            .header(
                                    "Authorization",
                                    "Bearer "
                                            + accessToken.trim()
                            )
                            .header(
                                    "Accept",
                                    "application/vnd.github+json"
                            )
                            .header(
                                    "X-GitHub-Api-Version",
                                    "2022-11-28"
                            )
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<
                                            List<GitHubEmailResponse>
                                            >() {
                                    }
                            );
        } catch (OAuth2AuthenticationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "github_email_request_failed",
                            "Não foi possível consultar "
                                    + "os e-mails do GitHub.",
                            null
                    ),
                    exception
            );
        }

        if (
                emails == null
                        || emails.isEmpty()
        ) {
            throw emailNotFoundException();
        }

        return emails
                .stream()
                .filter(
                        GitHubEmailResponse::primary
                )
                .filter(
                        GitHubEmailResponse::verified
                )
                .map(
                        GitHubEmailResponse::email
                )
                .filter(
                        email ->
                                email != null
                                        && !email.isBlank()
                )
                .map(
                        email ->
                                email
                                        .trim()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                )
                .findFirst()
                .orElseThrow(
                        this::emailNotFoundException
                );
    }

    private OAuth2AuthenticationException
    emailNotFoundException() {
        return authenticationException(
                "github_email_not_found",
                "O GitHub não disponibilizou "
                        + "um e-mail principal verificado."
        );
    }

    private OAuth2AuthenticationException
    authenticationException(
            String code,
            String description
    ) {
        OAuth2Error error =
                new OAuth2Error(
                        code,
                        description,
                        null
                );

        return new OAuth2AuthenticationException(
                error,
                description
        );
    }

    public record GitHubEmailResponse(
            String email,
            boolean primary,
            boolean verified,
            String visibility
    ) {
    }
}