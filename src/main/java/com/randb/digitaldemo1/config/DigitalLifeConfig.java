package com.randb.digitaldemo1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数字生命配置类
 * @author: randb
 * @date: 2025-08-22
 */
@Data
@Component
@ConfigurationProperties(prefix = "digital-life")
public class DigitalLifeConfig {
    
    /**
     * 自动启动配置
     */
    private AutoStart autoStart = new AutoStart();
    
    @Data
    public static class AutoStart {
        /**
         * 是否启用自动启动
         */
        private boolean enabled = false;
        
        /**
         * 启动延迟秒数
         */
        private int delaySeconds = 5;
    }
}
