package com.tola.sentinelvault.identity.infrastructure.web;

import com.tola.sentinelvault.identity.config.RefreshTokenCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieFactory {

    private final RefreshTokenCookieProperties properties;

    public ResponseCookie buildTokenCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getName(), token)
                .httpOnly(properties.isHttpOnly())
                .secure(properties.isSecure())
                .path(properties.getPath())
                .sameSite(properties.getSameSite())
                .maxAge(properties.getMaxAgeSeconds());

        if (properties.getDomain() != null && !properties.getDomain().isBlank()) {
            builder.domain(properties.getDomain());
        }

        return builder.build();
    }

    public ResponseCookie clearCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getName(), "")
                .httpOnly(properties.isHttpOnly())
                .secure(properties.isSecure())
                .path(properties.getPath())
                .sameSite(properties.getSameSite())
                .maxAge(0);

        if (properties.getDomain() != null && !properties.getDomain().isBlank()) {
            builder.domain(properties.getDomain());
        }
        return builder.build();
    }

    public Optional<String> extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> properties.getName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst();
    }
}
