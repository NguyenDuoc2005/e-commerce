package com.ecommerce.auth.security;

public final class LoginRoleContext {

    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    private LoginRoleContext() {
    }

    public static void set(String role) {
        ROLE.set(role);
    }

    public static String get() {
        return ROLE.get();
    }

    public static void clear() {
        ROLE.remove();
    }
}
