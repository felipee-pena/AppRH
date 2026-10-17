package com.evomanex.AppRH.events;

public record EmailVerificationRequestedEvent(
        String destinationEmail,
        String rawToken
) {
}