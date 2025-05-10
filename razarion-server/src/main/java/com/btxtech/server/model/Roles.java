package com.btxtech.server.model;

public final class Roles {

    public static final String ADMIN = "ADMIN";

    private Roles() {
    }

    public static String toJwtRole(String role) {
        return "ROLE_" + role;
    }
}
