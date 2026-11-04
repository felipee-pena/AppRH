package com.evomanex.AppRH.infra.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SecurityTestController {

    @PostMapping(
            "/auth/register"
    )
    public String register(
            @RequestBody
            Map<String, Object> request
    ) {
        return "registration";
    }

    @PostMapping(
            "/auth/email/resend"
    )
    public String resendEmail(
            @RequestBody
            Map<String, Object> request
    ) {
        return "email-resend";
    }

    @PostMapping(
            "/auth/oauth/exchange"
    )
    public String exchangeOAuthCode(
            @RequestBody
            Map<String, Object> request
    ) {
        return "oauth-exchange";
    }

    @PostMapping(
            "/auth/mfa/verify"
    )
    public String verifyMfa(
            @RequestBody
            Map<String, Object> request
    ) {
        return "mfa-verification";
    }

    @GetMapping(
            "/api/companies"
    )
    public String companies() {
        return "companies";
    }

    @GetMapping(
            "/api/jobs"
    )
    public String jobs() {
        return "jobs";
    }

    @GetMapping(
            "/api/rh/company"
    )
    public String rhCompany() {
        return "rh-company";
    }

    @GetMapping(
            "/api/admin/users"
    )
    public String administration() {
        return "administration";
    }

    @PostMapping(
            "/api/account/mfa/totp/setup"
    )
    public String mfaSetup() {
        return "mfa-setup";
    }
}