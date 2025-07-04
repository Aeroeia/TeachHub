package com.teachub.media.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.teachub.media.storage.IFileStorage;
import com.teachub.media.storage.ali.AliFileStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliProperties.class)
public class AliConfig {

    @Bean
    @ConditionalOnProperty(prefix = "teachub.file", name = "platform", havingValue = "ALI")
    public OSS aliOssClient(AliProperties prop){
        return new OSSClientBuilder()
                .build(prop.getOos().getEndpoint(), prop.getAccessId(), prop.getAccessKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "teachub.file", name = "platform", havingValue = "ALI")
    public IFileStorage aliFileStorage(OSS aliOssClient, AliProperties prop) {
        return new AliFileStorage(aliOssClient, prop.getOos().getBucket());
    }
}