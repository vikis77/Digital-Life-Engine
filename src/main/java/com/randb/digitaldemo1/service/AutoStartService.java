package com.randb.digitaldemo1.service;

import com.randb.digitaldemo1.config.DigitalLifeConfig;
import com.randb.digitaldemo1.core.DigitalLifeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自动启动服务
 * 在应用启动后自动启动数字生命
 * @author: randb
 * @date: 2025-08-22
 */
@Service
@Slf4j
public class AutoStartService {

    @Autowired
    private DigitalLifeConfig digitalLifeConfig;
    
    @Autowired
    private DigitalLifeEngine digitalLifeEngine;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 应用启动完成后的事件监听
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (digitalLifeConfig.getAutoStart().isEnabled()) {
            int delaySeconds = digitalLifeConfig.getAutoStart().getDelaySeconds();
            
            log.info("数字生命自动启动已启用，将在 {} 秒后启动", delaySeconds);
            
            scheduler.schedule(() -> {
                try {
                    log.info("开始自动启动数字生命...");
                    
                    if (digitalLifeEngine.isRunning()) {
                        log.warn("数字生命已经在运行中，跳过自动启动");
                        return;
                    }
                    
                    // 异步启动数字生命
                    CompletableFuture.runAsync(() -> {
                        digitalLifeEngine.startDigitalLife();
                    });
                    
                    log.info("数字生命自动启动完成");
                    
                } catch (Exception e) {
                    log.error("数字生命自动启动失败: {}", e.getMessage(), e);
                }
            }, delaySeconds, TimeUnit.SECONDS);
            
        } else {
            log.info("数字生命自动启动已禁用");
        }
    }
    
    /**
     * 应用关闭时清理资源
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
