package com.manager.auth_service.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ERole {
    ROLE_USER("Usuario"),
    ROLE_ADMIN("Administrador");

    private final String displayName;

    ERole(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue // Esto hará que en el JSON aparezca "Usuario"
    public String getDisplayName() {
        return displayName;
    }
}