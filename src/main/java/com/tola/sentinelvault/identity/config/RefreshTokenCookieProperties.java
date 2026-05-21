package com.tola.sentinelvault.identity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt.refresh-cookie")
public class RefreshTokenCookieProperties {

    private String name = "refresh_token";
    private String path = "/api";
    private String domain;
    private boolean secure = false;
    private boolean httpOnly = true;
    private String sameSite = "Lax";
    private long maxAgeSeconds = 604800;
}
