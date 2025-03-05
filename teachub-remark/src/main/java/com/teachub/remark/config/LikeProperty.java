package com.teachub.remark.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "tj.like")
@Data
public class LikeProperty {
    private List<String> bizTypes;
}
