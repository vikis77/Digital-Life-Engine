package com.randb.digitaldemo1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 读取配置文件Spring ai chat starter的配置
 * @author: randb
 * @date: 2025-08-22
 */
@Component
@Data
@ConfigurationProperties(prefix = "spring.ai.dashscope.chat.options")
public class SpringAIChatStarterConfig {

    /**
     * 模型名称
     */
    private String model;

    
}
