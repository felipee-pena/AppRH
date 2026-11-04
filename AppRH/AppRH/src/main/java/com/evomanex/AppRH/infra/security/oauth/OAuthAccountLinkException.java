package com.evomanex.AppRH.infra.security.oauth;

public class OAuthAccountLinkException
        extends RuntimeException {

    public OAuthAccountLinkException(
            String message
    ) {
        super(
                message
        );
    }

    public OAuthAccountLinkException(
            String message,
            Throwable cause
    ) {
        super(
                message,
                cause
        );
    }
}