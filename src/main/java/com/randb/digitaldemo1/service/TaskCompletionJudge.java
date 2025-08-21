package com.randb.digitaldemo1.service;

import com.alibaba.fastjson.JSONObject;
import com.randb.digitaldemo1.config.SpringAIChatStarterConfig;
import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 任务完成判断服务
 * 职责：专门判断任务是否应该完成，避免执行LLM的思维惯性
 * @author: randb
 * @date: 2025-08-22
 */
@Slf4j
@Service
public class TaskCompletionJudge {

    @Autowired
    private ChatModelFactory chatModelFactory;
    @Autowired
    private SpringAIChatStarterConfig springAIChatStarterConfig;

    /**
     * 判断任务是否应该完成（通用方法）
     * @param taskName 任务名称
     * @param executionHistory 执行历史
     * @param lastResponse 最后一次响应
     * @return true表示应该完成任务
     */
    public boolean shouldCompleteTask(String taskName, String executionHistory, String lastResponse) {
        try {
            log.info("任务完成判断：开始分析任务 '{}'", taskName);
            
            // 构建专门的判断prompt
            String judgePrompt = buildJudgePrompt(taskName, executionHistory, lastResponse);
            
            // 调用专门的判断LLM
            ChatService chatService = chatModelFactory.get(springAIChatStarterConfig.getModel());
            ChatRequest request = new ChatRequest();
            request.setRequestId(String.valueOf(System.currentTimeMillis()));
            request.setUserId("task_judge");
            request.setSessionId("task_judge_session");
            request.setModel(springAIChatStarterConfig.getModel());
            request.setStream(false);
            request.setPrompt(judgePrompt);

            ChatResponse response = chatService.syncReply(request);
            String llmResponse = response.getContent();
            log.info("任务完成判断：LLM响应 {}", llmResponse);
            
            // 解析判断结果
            return parseJudgeResult(llmResponse);
            
        } catch (Exception e) {
            log.error("任务完成判断失败: {}", e.getMessage(), e);
            // 默认不完成，避免意外结束
            return false;
        }
    }

    /**
     * 构建任务完成判断的专用prompt
     */
    private String buildJudgePrompt(String taskName, String executionHistory, String lastResponse) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专门的任务完成判断助手。你的唯一职责是判断任务是否已经完成。\n\n");
        
        prompt.append("任务名称：").append(taskName).append("\n\n");
        
        prompt.append("执行历史：\n").append(executionHistory).append("\n\n");
        
        prompt.append("最后一次操作响应：\n").append(lastResponse).append("\n\n");
        
        prompt.append("判断规则：\n");
        prompt.append("1. 仔细分析任务名称的核心要求\n");
        prompt.append("2. 检查执行历史是否已经满足任务要求\n");
        prompt.append("3. 特别注意：如果任务是'发布帖子'且已经发布成功，应该完成\n");
        prompt.append("4. 特别注意：如果任务是'审核'且已经审核完成，应该完成\n");
        prompt.append("5. 特别注意：如果查询类操作返回空数据，通常应该完成\n");
        prompt.append("6. 避免无限循环：如果核心任务已完成，不要因为可以做更多事情就不结束\n\n");
        
        prompt.append("请严格按照以下JSON格式回复：\n");
        prompt.append("{\n");
        prompt.append("  \"should_complete\": true/false,\n");
        prompt.append("  \"reason\": \"判断理由\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    /**
     * 解析判断结果
     */
    private boolean parseJudgeResult(String llmResponse) {
        try {
            // 提取JSON部分
            String jsonStr = extractJsonFromResponse(llmResponse);
            if (jsonStr == null) {
                log.warn("无法从LLM响应中提取JSON: {}", llmResponse);
                return false;
            }
            
            JSONObject result = JSONObject.parseObject(jsonStr);
            boolean shouldComplete = result.getBooleanValue("should_complete");
            String reason = result.getString("reason");
            
            log.info("任务完成判断结果：{}, 理由：{}", shouldComplete, reason);
            
            return shouldComplete;
            
        } catch (Exception e) {
            log.error("解析判断结果失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从响应中提取JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        if (response == null) return null;
        
        // 查找JSON开始和结束位置
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return null;
    }

    /**
     * 构建执行历史摘要
     */
    public String buildExecutionHistory(String taskName, int currentStep, String lastStepResult) {
        StringBuilder history = new StringBuilder();
        
        history.append("任务：").append(taskName).append("\n");
        history.append("当前步骤：第").append(currentStep).append("步\n");
        
        if (lastStepResult != null) {
            history.append("最后一步结果：").append(lastStepResult).append("\n");
        }
        
        return history.toString();
    }
}
