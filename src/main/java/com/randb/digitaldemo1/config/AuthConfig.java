package com.randb.digitaldemo1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证配置类
 * 用于读取永久token配置
 * @author: randb
 * @date: 2025-08-22
 */
@Data
@Component
@ConfigurationProperties(prefix = "digital-life.auth")
public class AuthConfig {
    
    /**
     * 永久认证token
     * 用户可以在配置文件中设置，避免每次都需要登录
     */
    private String permanentToken;
    
    /**
     * 检查是否配置了永久token
     * @return 是否有有效的永久token
     */
    public boolean hasPermanentToken() {
        return permanentToken != null && !permanentToken.trim().isEmpty();
    }
    
    /**
     * 获取永久token（去除空格和Bearer前缀）
     * @return 永久token，如果没有配置则返回null
     */
    public String getPermanentToken() {
        if (hasPermanentToken()) {
            String token = permanentToken.trim();
            // 如果配置中包含Bearer前缀，去除它
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return token;
        }
        return null;
    }
}
