package com.tola.sentinelvault.platform.ratelimit;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "sentinelvault.rate-limit")
public class RateLimitProperties {

    @Min(1)
    private long maxAttempts = 20;

    @Min(1)
    private long windowSeconds = 900;
}
