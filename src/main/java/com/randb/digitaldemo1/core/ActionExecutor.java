package com.randb.digitaldemo1.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.randb.digitaldemo1.entity.HttpRequestInfo;
import com.randb.digitaldemo1.service.StateManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 动作执行器
 * 解析和执行LLM生成的动作指令
 * @author: randb
 * @date: 2025-08-22
 */
@Slf4j
public class ActionExecutor {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();
    private static StateManager stateManager;

    /**
     * 设置状态管理器
     * @param manager 状态管理器实例
     */
    public static void setStateManager(StateManager manager) {
        stateManager = manager;
    }

    /**
     * 新的动作执行器 - 解析LLM生成的复杂动作指令
     * @param actionInstructionJson LLM生成的动作指令JSON字符串
     */
    public static void executeComplexAction(String actionInstructionJson) {
        try {
            log.info("开始解析动作指令: {}", actionInstructionJson);
            
            // 解析JSON
            JsonNode rootNode = objectMapper.readTree(actionInstructionJson);
            
            // 遍历所有动作类型（如"发布一个帖子"）
            rootNode.fieldNames().forEachRemaining(actionType -> {
                try {
                    log.info("执行动作类型: {}", actionType);
                    JsonNode actionNode = rootNode.get(actionType);
                    
                    if (actionNode.has("步骤")) {
                        // 标准格式：包含步骤数组
                        JsonNode stepsNode = actionNode.get("步骤");
                        
                        // 遍历每个步骤
                        for (int i = 0; i < stepsNode.size(); i++) {
                            JsonNode stepNode = stepsNode.get(i);
                            executeStep(stepNode, i + 1);
                        }
                    } else if (actionNode.has("url") || actionNode.has("method")) {
                        // 简单格式：直接包含HTTP请求信息
                        log.info("执行简单动作: {}", actionType);
                        HttpRequestInfo httpInfo = extractHttpRequestInfo(actionNode);
                        
                        if (httpInfo != null && httpInfo.isValid()) {
                            sendHttpRequest(httpInfo);
                        } else {
                            log.warn("简单动作 {} 没有有效的HTTP请求信息", actionType);
                        }
                    } else {
                        // 检查是否是嵌套的动作对象
                        executeNestedAction(actionNode, actionType);
                    }
                } catch (Exception e) {
                    log.error("执行动作类型 {} 失败: {}", actionType, e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            log.error("解析动作指令失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析动作指令失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行单个步骤
     */
    private static void executeStep(JsonNode stepNode, int stepNumber) {
        try {
            String description = stepNode.has("描述") ? stepNode.get("描述").asText() : "";
            log.info("执行步骤 {}: {}", stepNumber, description);
            
            // 检查是否有动作节点
            if (stepNode.has("动作") && !stepNode.get("动作").isTextual()) {
                JsonNode actionNode = stepNode.get("动作");
                
                // 提取HTTP请求信息
                HttpRequestInfo httpInfo = extractHttpRequestInfo(actionNode);
                
                if (httpInfo != null && httpInfo.isValid()) {
                    // 发送HTTP请求
                    sendHttpRequest(httpInfo);
                } else {
                    log.info("步骤 {} 没有有效的HTTP请求信息", stepNumber);
                }
            } else {
                log.info("步骤 {} 没有动作或动作为空字符串", stepNumber);
            }
            
            // 检查是否为最后步骤
            if (stepNode.has("是否最后步骤")) {
                String isLastStep = stepNode.get("是否最后步骤").asText();
                if ("是".equals(isLastStep)) {
                    log.info("已到达最后步骤");
                }
            }
            
        } catch (Exception e) {
            log.error("执行步骤 {} 失败: {}", stepNumber, e.getMessage(), e);
        }
    }

    /**
     * 执行嵌套的动作对象
     */
    private static void executeNestedAction(JsonNode actionNode, String actionType) {
        try {
            // 遍历嵌套对象的所有字段
            actionNode.fieldNames().forEachRemaining(fieldName -> {
                try {
                    JsonNode fieldNode = actionNode.get(fieldName);
                    
                    if (fieldNode.has("url") || fieldNode.has("method")) {
                        // 找到HTTP请求信息
                        log.info("执行嵌套动作: {} -> {}", actionType, fieldName);
                        HttpRequestInfo httpInfo = extractHttpRequestInfo(fieldNode);
                        
                        if (httpInfo != null && httpInfo.isValid()) {
                            sendHttpRequest(httpInfo);
                        } else {
                            log.warn("嵌套动作 {} -> {} 没有有效的HTTP请求信息", actionType, fieldName);
                        }
                    }
                } catch (Exception e) {
                    log.error("执行嵌套动作字段 {} 失败: {}", fieldName, e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("执行嵌套动作 {} 失败: {}", actionType, e.getMessage(), e);
        }
    }

    /**
     * 从动作节点中提取HTTP请求信息
     */
    private static HttpRequestInfo extractHttpRequestInfo(JsonNode actionNode) {
        try {
            HttpRequestInfo info = new HttpRequestInfo();
            
            if (actionNode.has("method")) {
                info.setMethod(actionNode.get("method").asText());
            }
            
            if (actionNode.has("url")) {
                info.setUrl(actionNode.get("url").asText());
            }
            
            if (actionNode.has("params")) {
                JsonNode paramsNode = actionNode.get("params");
                @SuppressWarnings("unchecked")
                Map<String, Object> paramsMap = objectMapper.convertValue(paramsNode, Map.class);
                info.setParams(paramsMap);
            }
            
            if (actionNode.has("body")) {
                JsonNode bodyNode = actionNode.get("body");
                if (bodyNode.isObject()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bodyMap = objectMapper.convertValue(bodyNode, Map.class);
                    info.setBody(bodyMap);
                } else {
                    info.setBodyString(bodyNode.asText());
                }
            }
            
            return info;
        } catch (Exception e) {
            log.error("提取HTTP请求信息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 发送HTTP请求（增强版，支持状态管理）
     */
    private static void sendHttpRequest(HttpRequestInfo httpInfo) {
        try {
            log.info("发送HTTP请求: {} {}", httpInfo.getMethod(), httpInfo.getUrl());
            log.info("请求参数: {}", httpInfo.getParams());
            log.info("请求体: {}", httpInfo.getBody() != null ? httpInfo.getBody() : httpInfo.getBodyString());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 如果有登录token，添加到请求头
            if (stateManager != null && stateManager.hasState("login_token")) {
                String token = stateManager.getLoginToken();
                headers.setBearerAuth(token);
                log.info("添加认证token到请求头");
            }
            
            // 准备请求体
            Object requestBody = null;
            if (httpInfo.getBody() != null) {
                requestBody = httpInfo.getBody();
            } else if (httpInfo.getBodyString() != null && !httpInfo.getBodyString().isEmpty()) {
                requestBody = httpInfo.getBodyString();
            }
            
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            HttpMethod method = HttpMethod.valueOf(httpInfo.getMethod().toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(
                httpInfo.getUrl(),
                method,
                entity,
                String.class
            );
            
            log.info("HTTP请求响应状态: {}", response.getStatusCode());
            log.info("HTTP请求响应内容: {}", response.getBody());
            
            // 保存响应到状态管理器
            if (stateManager != null) {
                stateManager.saveLastResponse(response.getBody());
                
                // 如果是登录请求，尝试提取token
                if (httpInfo.getUrl().contains("login") && response.getStatusCode().is2xxSuccessful()) {
                    extractAndSaveToken(response.getBody());
                }
            }
            
        } catch (Exception e) {
            log.error("发送HTTP请求失败: {}", e.getMessage(), e);
            if (stateManager != null) {
                stateManager.saveLastResponse("ERROR: " + e.getMessage());
            }
        }
    }
    
    /**
     * 从登录响应中提取并保存token
     */
    private static void extractAndSaveToken(String responseBody) {
        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                JsonNode responseNode = objectMapper.readTree(responseBody);
                
                // 首先检查是否有data字段（嵌套结构）
                if (responseNode.has("data")) {
                    JsonNode dataNode = responseNode.get("data");
                    if (dataNode.has("token")) {
                        String token = dataNode.get("token").asText();
                        if (token != null && !token.isEmpty()) {
                            stateManager.saveLoginToken(token);
                            log.info("成功提取并保存登录token");
                            return;
                        }
                    }
                }
                
                // 尝试多种可能的顶级token字段名
                String[] tokenFields = {"token", "accessToken", "access_token", "authToken", "jwt"};
                
                for (String field : tokenFields) {
                    if (responseNode.has(field)) {
                        String token = responseNode.get(field).asText();
                        if (token != null && !token.isEmpty()) {
                            stateManager.saveLoginToken(token);
                            log.info("成功提取并保存登录token");
                            return;
                        }
                    }
                }
                
                log.warn("未能从登录响应中提取token");
            }
        } catch (Exception e) {
            log.error("提取token失败: {}", e.getMessage(), e);
        }
    }
}
