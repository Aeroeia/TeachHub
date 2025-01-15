package com.teachub.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "teachub.interests")
public class InterestsProperties {
    private int topNumber;
}
