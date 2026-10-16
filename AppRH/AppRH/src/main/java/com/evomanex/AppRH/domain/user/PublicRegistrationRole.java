package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Perfis disponíveis no cadastro público"
)
public enum PublicRegistrationRole {

    USER,
    USERRH
}