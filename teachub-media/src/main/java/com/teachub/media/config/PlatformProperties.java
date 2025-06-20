package com.teachub.media.config;

import com.teachub.media.enums.Platform;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "teachub.platform")
public class PlatformProperties {
    private Platform file;
    private Platform media;
}
