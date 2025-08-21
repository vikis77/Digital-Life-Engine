package com.randb.digitaldemo1.controller;

import com.randb.digitaldemo1.core.DigitalLifeEngine;
import com.randb.digitaldemo1.service.StateManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * 数字生命控制器
 * 提供启动、停止、状态查询等接口
 * @author: randb
 * @date: 2025-08-22
 */
@RestController
@RequestMapping("/api/digital-life")
@Slf4j
public class DigitalLifeController {

    @Autowired
    private DigitalLifeEngine digitalLifeEngine;
    
    @Autowired
    private StateManager stateManager;

    /**
     * 启动数字生命
     */
    @PostMapping("/start")
    public String startDigitalLife() {
        try {
            if (digitalLifeEngine.isRunning()) {
                return "数字生命已经在运行中";
            }
            
            // 异步启动数字生命，避免阻塞HTTP请求
            CompletableFuture.runAsync(() -> {
                digitalLifeEngine.startDigitalLife();
            });
            
            return "数字生命启动成功";
        } catch (Exception e) {
            log.error("启动数字生命失败: {}", e.getMessage(), e);
            return "启动数字生命失败: " + e.getMessage();
        }
    }

    /**
     * 停止数字生命
     */
    @PostMapping("/stop")
    public String stopDigitalLife() {
        try {
            digitalLifeEngine.stopDigitalLife();
            return "数字生命停止信号已发送";
        } catch (Exception e) {
            log.error("停止数字生命失败: {}", e.getMessage(), e);
            return "停止数字生命失败: " + e.getMessage();
        }
    }

    /**
     * 获取数字生命状态
     */
    @GetMapping("/status")
    public String getStatus() {
        try {
            return digitalLifeEngine.getStatusInfo();
        } catch (Exception e) {
            log.error("获取状态失败: {}", e.getMessage(), e);
            return "获取状态失败: " + e.getMessage();
        }
    }

    /**
     * 获取所有状态信息
     */
    @GetMapping("/states")
    public Object getAllStates() {
        try {
            return stateManager.getAllStates();
        } catch (Exception e) {
            log.error("获取所有状态失败: {}", e.getMessage(), e);
            return "获取所有状态失败: " + e.getMessage();
        }
    }

    /**
     * 清空所有状态
     */
    @PostMapping("/clear-states")
    public String clearAllStates() {
        try {
            stateManager.clearAllStates();
            return "所有状态已清空";
        } catch (Exception e) {
            log.error("清空状态失败: {}", e.getMessage(), e);
            return "清空状态失败: " + e.getMessage();
        }
    }

    /**
     * 手动设置状态
     */
    @PostMapping("/set-state")
    public String setState(@RequestParam String key, @RequestParam String value) {
        try {
            stateManager.saveState(key, value);
            return "状态设置成功: " + key + " = " + value;
        } catch (Exception e) {
            log.error("设置状态失败: {}", e.getMessage(), e);
            return "设置状态失败: " + e.getMessage();
        }
    }

    /**
     * 获取特定状态
     */
    @GetMapping("/get-state")
    public String getState(@RequestParam String key) {
        try {
            Object value = stateManager.getState(key);
            return key + " = " + (value != null ? value.toString() : "null");
        } catch (Exception e) {
            log.error("获取状态失败: {}", e.getMessage(), e);
            return "获取状态失败: " + e.getMessage();
        }
    }

    /**
     * 手动完成当前任务
     */
    @PostMapping("/complete-task")
    public String completeCurrentTask() {
        try {
            String currentTask = stateManager.getCurrentTask();
            if (currentTask != null) {
                stateManager.removeState("current_task");
                stateManager.removeState("current_step");
                stateManager.removeState("current_step_result");
                stateManager.removeState("next_step");
                log.info("✅ 手动完成任务: {}", currentTask);
                return "任务已手动完成: " + currentTask + "，系统将选择新任务";
            } else {
                return "当前没有正在执行的任务";
            }
        } catch (Exception e) {
            log.error("手动完成任务失败: {}", e.getMessage(), e);
            return "手动完成任务失败: " + e.getMessage();
        }
    }

    /**
     * 重置系统状态
     */
    @PostMapping("/reset")
    public String resetSystem() {
        try {
            stateManager.clearAllStates();
            log.info("🔄 系统状态已重置");
            return "系统状态已重置，将重新开始选择任务";
        } catch (Exception e) {
            log.error("重置系统失败: {}", e.getMessage(), e);
            return "重置系统失败: " + e.getMessage();
        }
    }

    /**
     * 获取登录token状态
     */
    @GetMapping("/token-status")
    public String getTokenStatus() {
        try {
            String token = stateManager.getLoginToken();
            if (token != null) {
                return "已登录，Token: " + token.substring(0, Math.min(20, token.length())) + "...";
            } else {
                return "未登录，没有Token";
            }
        } catch (Exception e) {
            return "获取Token状态失败: " + e.getMessage();
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        return "数字生命系统正常运行";
    }
}
