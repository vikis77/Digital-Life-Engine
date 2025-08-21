package com.randb.digitaldemo1.service;

import com.randb.digitaldemo1.config.AuthConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 数字生命状态管理器
 * 用于保存执行过程中的状态信息，如token、响应结果等
 * @author: randb
 * @date: 2025-08-22
 */
@Component
@Slf4j
public class StateManager {

    @Autowired
    private AuthConfig authConfig;

    // 使用内存存储状态信息（可以后续扩展为Redis、数据库等）
    private final Map<String, Object> stateStore = new ConcurrentHashMap<>();
    
    /**
     * 保存状态
     * @param key 状态键
     * @param value 状态值
     */
    public void saveState(String key, Object value) {
        stateStore.put(key, value);
        log.info("保存状态: {} = {}", key, value);
    }
    
    /**
     * 获取状态
     * @param key 状态键
     * @return 状态值
     */
    public Object getState(String key) {
        Object value = stateStore.get(key);
        log.info("获取状态: {} = {}", key, value);
        return value;
    }
    
    /**
     * 获取字符串状态
     * @param key 状态键
     * @return 字符串状态值
     */
    public String getStringState(String key) {
        Object value = getState(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 检查状态是否存在
     * @param key 状态键
     * @return 是否存在
     */
    public boolean hasState(String key) {
        // 对于login_token，优先检查永久token
        if ("login_token".equals(key)) {
            return hasLoginToken();
        }

        return stateStore.containsKey(key);
    }

    /**
     * 检查是否有可用的登录token（永久token或动态token）
     * @return 是否有可用的token
     */
    public boolean hasLoginToken() {
        // 优先检查永久token
        if (authConfig != null && authConfig.hasPermanentToken()) {
            return true;
        }

        // 检查动态token
        return stateStore.containsKey("login_token");
    }
    
    /**
     * 删除状态
     * @param key 状态键
     */
    public void removeState(String key) {
        Object removed = stateStore.remove(key);
        log.info("删除状态: {} = {}", key, removed);
    }
    
    /**
     * 清空所有状态
     */
    public void clearAllStates() {
        stateStore.clear();
        log.info("清空所有状态");
    }
    
    /**
     * 获取所有状态
     * @return 状态映射
     */
    public Map<String, Object> getAllStates() {
        return new ConcurrentHashMap<>(stateStore);
    }
    
    /**
     * 保存登录token
     * @param token 登录token
     */
    public void saveLoginToken(String token) {
        saveState("login_token", token);
    }
    
    /**
     * 获取登录token（优先使用永久token）
     * @return 登录token
     */
    public String getLoginToken() {
        // 优先使用配置文件中的永久token
        if (authConfig != null && authConfig.hasPermanentToken()) {
            String permanentToken = authConfig.getPermanentToken();
            log.debug("使用配置文件中的永久token");
            return permanentToken;
        }

        // 如果没有永久token，使用动态获取的token
        String dynamicToken = getStringState("login_token");
        if (dynamicToken != null) {
            log.debug("使用动态获取的token");
            return dynamicToken;
        }

        log.warn("没有找到任何可用的token");
        return null;
    }
    
    /**
     * 保存最后一次HTTP响应
     * @param response 响应内容
     */
    public void saveLastResponse(String response) {
        saveState("last_response", response);
    }
    
    /**
     * 获取最后一次HTTP响应
     * @return 响应内容
     */
    public String getLastResponse() {
        return getStringState("last_response");
    }
    
    /**
     * 保存当前任务状态
     * @param taskName 任务名称
     * @param stepIndex 当前步骤索引
     */
    public void saveTaskProgress(String taskName, int stepIndex) {
        saveState("current_task", taskName);
        saveState("current_step", stepIndex);
    }
    
    /**
     * 获取当前任务名称
     * @return 任务名称
     */
    public String getCurrentTask() {
        return getStringState("current_task");
    }
    
    /**
     * 获取当前步骤索引
     * @return 步骤索引
     */
    public Integer getCurrentStep() {
        Object step = getState("current_step");
        return step != null ? (Integer) step : 0;
    }
}
