package com.teachub.api.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.teachub.api.cache.RoleCache;
import com.teachub.api.client.auth.AuthClient;
import com.teachub.api.dto.auth.RoleDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RoleCacheConfig {

    @DubboReference
    private AuthClient authClient;

    /**
     * 角色的caffeine缓存
     */
    @Bean
    public Cache<Long, RoleDTO> roleCaches(){
        return Caffeine.newBuilder()
                .initialCapacity(1)
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();
    }
    /**
     * 角色的缓存工具
     */
    @Bean
    public RoleCache roleCache(Cache<Long, RoleDTO> roleCaches){
        return new RoleCache(roleCaches, authClient);
    }
}
