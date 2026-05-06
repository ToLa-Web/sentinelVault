package com.tola.sentinelvault.platform.util;

/**
 * Application-wide constants.
 * No instances — use statically.
 */
public final class Constants {

    private Constants() {}

    public static final String API_PREFIX       = "/api";
    public static final String AUTH_PREFIX      = "/api/auth";
    public static final String SECRETS_PREFIX   = "/api/secrets";

    public static final String ROLE_ADMIN       = "ROLE_ADMIN";
    public static final String ROLE_MEMBER      = "ROLE_MEMBER";
    public static final String ROLE_VIEWER      = "ROLE_VIEWER";

    public static final String JWT_HEADER       = "Authorization";
    public static final String JWT_PREFIX       = "Bearer ";
}