package com.algorena.users.domain;

public enum Role {
    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}