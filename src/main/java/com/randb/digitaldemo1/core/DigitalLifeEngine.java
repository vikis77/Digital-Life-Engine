package com.randb.digitaldemo1.core;

import com.alibaba.fastjson.JSONObject;
import com.randb.digitaldemo1.config.DigitalLifeConfig;
import com.randb.digitaldemo1.config.SpringAIChatStarterConfig;
import com.randb.digitaldemo1.entity.Prompt;
import com.randb.digitaldemo1.service.ActionFormatter;
import com.randb.digitaldemo1.service.StateManager;
import com.randb.digitaldemo1.service.TaskCompletionJudge;

import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * 数字生命核心引擎
 * 实现完整的数字生命循环逻辑
 * @author: randb
 * @date: 2025-08-22
 */
@Component
@Slf4j
public class DigitalLifeEngine {

    @Autowired
    private ChatModelFactory chatModelFactory;
    @Autowired
    private StateManager stateManager;
    @Autowired
    private ActionFormatter actionFormatter;
    @Autowired
    private TaskCompletionJudge taskCompletionJudge;
    @Autowired
    private SpringAIChatStarterConfig springAIChatStarterConfig;


    private final Random random = new Random();
    
    // 数字生命是否运行中
    private volatile boolean isRunning = false;
    
    /**
     * 启动数字生命
     */
    public void startDigitalLife() {
        if (isRunning) {
            log.warn("数字生命已经在运行中");
            return;
        }
        
        isRunning = true;
        log.info("🚀 启动数字生命引擎...");
        
        // 设置ActionExecutor的状态管理器
        ActionExecutor.setStateManager(stateManager);
        
        // 清空之前的状态
        stateManager.clearAllStates();
        
        try {
            // 开始数字生命循环
            digitalLifeLoop();
        } catch (Exception e) {
            log.error("数字生命运行异常: {}", e.getMessage(), e);
        } finally {
            isRunning = false;
            log.info("🛑 数字生命引擎停止");
        }
    }
    
    /**
     * 停止数字生命
     */
    public void stopDigitalLife() {
        isRunning = false;
        log.info("收到停止信号，数字生命将在当前任务完成后停止");
    }
    
    /**
     * 数字生命主循环
     */
    private void digitalLifeLoop() {
        ChatService chatService = chatModelFactory.get(springAIChatStarterConfig.getModel());
        int loopCount = 1;
        
        while (isRunning) {
            try {
                // 获取当前任务信息用于日志显示
                String currentTaskForLog = stateManager.getCurrentTask();
                Integer currentStepForLog = stateManager.getCurrentStep();
                
                if (currentTaskForLog != null) {
                    log.info("\n========== 数字生命循环 第{}轮 ========== [任务: {} | 第{}步]", 
                            loopCount, currentTaskForLog, currentStepForLog + 1);
                } else {
                    log.info("\n========== 数字生命循环 第{}轮 ========== [准备选择新任务]", loopCount);
                }
                
                // 1. 选择任务（如果没有当前任务）
                String currentTask = stateManager.getCurrentTask();
                if (currentTask == null) {
                    currentTask = selectRandomTask();
                    if (currentTask == null) {
                        // 没有可用任务，停止循环
                        log.info("没有可用任务，数字生命停止运行");
                        break;
                    }
                    stateManager.saveTaskProgress(currentTask, 0);

                    // 清空上一个任务的响应状态，避免影响新任务的判断
                    stateManager.removeState("last_response");
                    stateManager.removeState("current_step_result");
                    stateManager.removeState("next_step");

                    log.info("选择新任务: {}", currentTask);
                    log.info("已清空上一任务的状态信息");
                }
                
                // 2. 构建Prompt
                Prompt prompt = buildPrompt(currentTask);
                
                // 3. 调用LLM
                ChatRequest request = createChatRequest(prompt);
                ChatResponse response = chatService.syncReply(request);
                
                // 4. 解析LLM响应
                boolean taskCompleted = processLLMResponse(response, currentTask);
                
                // 5. 如果任务完成，清空当前任务状态，准备选择新任务
                if (taskCompleted) {
                    stateManager.removeState("current_task");
                    stateManager.removeState("current_step");
                    log.info("任务完成: {}", currentTask);
                    log.info("准备从tasks.txt随机选择新任务...");
                    
                    // 短暂休息后开始新任务
                    Thread.sleep(15000);
                } else {
                    // 任务未完成，继续下一步
                    Thread.sleep(8000);
                }
                
                loopCount++;
                
                // 防止无限循环，设置最大循环次数
                if (loopCount > 100) {
                    log.warn("达到最大循环次数，停止数字生命");
                    break;
                }
                
            } catch (Exception e) {
                log.error("数字生命循环异常: {}", e.getMessage(), e);
                try {
                    Thread.sleep(10000); // 异常时等待更长时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    /**
     * 随机选择任务
     */
    private String selectRandomTask() {
        try {
            String tasksContent = Files.readString(Paths.get("src/main/resources/tasks.txt"));
            if (tasksContent == null || tasksContent.trim().isEmpty()) {
                log.warn("tasks.txt文件为空，没有可执行的任务");
                stopDigitalLife();
                return null;
            }
            
            String[] tasks = tasksContent.split("；");
            
            // 过滤空任务
            String[] validTasks = Arrays.stream(tasks)
                    .map(String::trim)
                    .filter(task -> !task.isEmpty())
                    .toArray(String[]::new);
            
            if (validTasks.length > 0) {
                String selectedTask = validTasks[random.nextInt(validTasks.length)];
                log.info("随机选择任务: {}", selectedTask);
                return selectedTask;
            } else {
                log.warn("tasks.txt中没有有效的任务，停止数字生命");
                stopDigitalLife();
                return null;
            }
        } catch (IOException e) {
            log.error("读取任务文件失败: {}", e.getMessage(), e);
            log.info("由于无法读取任务文件，停止数字生命服务");
            stopDigitalLife();
            return null;
        }
    }
    
    /**
     * 构建Prompt
     */
    private Prompt buildPrompt(String currentTask) {
        Prompt prompt = new Prompt();
        
        // 设置基本信息
        prompt.setWhatIsMytask(currentTask);
        
        // 设置我刚刚做了什么（包含任务进度信息和数据内容）
        String lastResponse = stateManager.getLastResponse();
        Integer currentStep = stateManager.getCurrentStep();

        if (lastResponse != null) {
            // 分析响应内容，提供更好的上下文
            String contextInfo = analyzeLastResponse(lastResponse, currentTask, currentStep);

            // 提取关键数据信息
            String dataInfo = extractKeyDataFromResponse(lastResponse);
            log.info("调试：extractKeyDataFromResponse 返回结果: {}", dataInfo);
            if (dataInfo != null) {
                contextInfo += "\n" + dataInfo;
                log.info("调试：已添加数据信息到上下文");
            } else {
                log.info("调试：数据信息为null，未添加到上下文");
            }

            prompt.setWhatDidIJustDo(contextInfo);
        } else {
            prompt.setWhatDidIJustDo("刚开始执行任务: " + currentTask + "，当前是第1步");
        }
        
        // 设置下一步指导
        String guidance = buildGuidance(currentTask, currentStep, lastResponse);
        prompt.setWhatNow(guidance);
        
        return prompt;
    }
    
    /**
     * 分析上一步响应，提供更好的上下文（通用方法）
     */
    private String analyzeLastResponse(String response, String currentTask, Integer currentStep) {
        try {
            // 如果没有响应，说明是新任务的开始
            if (response == null || response.trim().isEmpty()) {
                return String.format("刚开始执行新任务: %s，准备执行第一步操作", currentTask);
            }

            // 构建基础上下文
            String baseContext = String.format("任务: %s，当前第%d步", currentTask, currentStep + 1);

            // 通用的响应分析
            ResponseAnalysis analysis = analyzeResponseContent(response);

            // 根据分析结果构建上下文
            if (analysis.isSuccess()) {
                String actionDescription = extractActionDescription(response, currentTask);
                if (analysis.hasData()) {
                    return baseContext + String.format("，上一步成功%s，获得了数据，可以继续下一步或完成任务", actionDescription);
                } else {
                    return baseContext + String.format("，上一步成功%s", actionDescription);
                }
            } else if (analysis.isError()) {
                return baseContext + "，上一步操作失败，需要重新尝试或调整策略";
            } else {
                // 无法明确判断的情况，提供通用描述
                return baseContext + "，上一步执行完成，请根据结果选择下一步操作";
            }
        } catch (Exception e) {
            return String.format("任务: %s，当前第%d步，上一步执行完成", currentTask, currentStep + 1);
        }
    }

    /**
     * 通用的响应内容分析
     */
    private ResponseAnalysis analyzeResponseContent(String response) {
        if (response == null) {
            return new ResponseAnalysis(false, false, false);
        }

        // 检查是否成功
        boolean isSuccess = response.contains("成功") ||
                           response.contains("success") ||
                           response.contains("code\":10001") ||
                           response.contains("\"code\":10001");

        // 检查是否有错误
        boolean isError = response.contains("错误") ||
                         response.contains("失败") ||
                         response.contains("error") ||
                         response.contains("code\":10002") ||
                         response.contains("\"code\":10002");

        // 检查是否包含数据（通用数据特征）
        boolean hasData = response.contains("data") ||
                         response.contains("postId") ||
                         response.contains("userId") ||
                         response.contains("id") ||
                         response.contains("list") ||
                         response.contains("items");

        return new ResponseAnalysis(isSuccess, isError, hasData);
    }

    /**
     * 从响应中提取动作描述（基于任务名称推断）
     */
    private String extractActionDescription(String response, String currentTask) {
        // 基于任务名称推断动作类型
        if (currentTask.contains("登录")) {
            return "登录";
        } else if (currentTask.contains("发布") || currentTask.contains("发帖")) {
            return "发布内容";
        } else if (currentTask.contains("浏览")) {
            return "浏览内容";
        } else if (currentTask.contains("评论")) {
            return "评论";
        } else if (currentTask.contains("查询") || currentTask.contains("获取")) {
            return "获取信息";
        } else {
            return "执行操作";
        }
    }

    /**
     * 响应分析结果类
     */
    private static class ResponseAnalysis {
        private final boolean success;
        private final boolean error;
        private final boolean hasData;

        public ResponseAnalysis(boolean success, boolean error, boolean hasData) {
            this.success = success;
            this.error = error;
            this.hasData = hasData;
        }

        public boolean isSuccess() { return success; }
        public boolean isError() { return error; }
        public boolean hasData() { return hasData; }
    }

    /**
     * 从响应中提取关键数据信息（完全通用方法）
     */
    private String extractKeyDataFromResponse(String response) {
        try {
            if (response == null || response.trim().isEmpty()) {
                return null;
            }

            // 尝试解析JSON响应
            JSONObject responseObj = JSONObject.parseObject(response);
            if (responseObj == null) {
                return null;
            }

            // 检查是否有data字段
            Object dataObj = responseObj.get("data");
            if (dataObj == null) {
                // 通用处理：根据msg字段判断data为null的含义
                Object msgObj = responseObj.get("msg");
                String msg = msgObj != null ? msgObj.toString() : null;

                log.info("调试：检测到data为null，msg: {}", msg);

                if (msg != null && !msg.trim().isEmpty()) {
                    // 有msg说明是操作成功的响应，data为null是正常的
                    log.info("调试：有msg内容，这是操作成功响应，data为null正常");
                    return "上一步获得的数据：\n```json\n" + response + "\n```\n\n操作成功：" + msg + "\n请基于操作结果进行下一步操作";
                } else {
                    // 没有msg说明是查询无结果，应该结束任务
                    log.info("调试：无msg内容，这是查询无结果，建议结束任务");
                    String emptyDataMessage = "上一步获得的数据：\n```json\n" + response + "\n```\n\n重要：查询成功但无数据返回，表示没有更多数据可处理\n建议将任务标记为完成(yes)，然后选择新任务";
                    log.info("调试：空数据提示内容: {}", emptyDataMessage);
                    return emptyDataMessage;
                }
            }

            // 通用的数据描述：直接展示原始JSON，让LLM自己理解
            StringBuilder dataInfo = new StringBuilder();
            dataInfo.append("上一步获得的数据：");

            if (dataObj instanceof JSONObject) {
                JSONObject data = (JSONObject) dataObj;

                // 方案1：直接展示原始JSON数据
                dataInfo.append("\n```json");
                dataInfo.append("\n").append(data.toJSONString());
                dataInfo.append("\n```");

                // 方案2：同时提供格式化的字段列表作为补充
                dataInfo.append("\n\n字段详情：");
                int fieldCount = 0;
                for (String key : data.keySet()) {
                    Object value = data.get(key);
                    if (value != null && fieldCount < 10) {
                        String valueStr = value.toString();
                        if (valueStr.length() > 50) {
                            valueStr = valueStr.substring(0, 50) + "...";
                        }
                        String typeHint = getFieldTypeHint(value);
                        dataInfo.append(String.format("\n- %s = %s %s", key, valueStr, typeHint));
                        fieldCount++;
                    }
                }

                dataInfo.append("\n\n请基于上述JSON数据进行下一步操作");
                dataInfo.append("\n使用参数时，请直接从JSON中获取对应字段的值");
            }

            return dataInfo.toString();

        } catch (Exception e) {
            log.debug("提取数据信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取字段类型提示（完全通用方法）
     */
    private String getFieldTypeHint(Object value) {
        if (value == null) {
            return "";
        }

        // 通用的类型识别，不特例化任何字段名
        if (value instanceof Number) {
            return "(数字)";
        } else if (value instanceof String) {
            String str = value.toString();
            if (str.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return "(时间)";
            } else if (str.length() > 20) {
                return "(文本)";
            } else {
                return "(字符串)";
            }
        } else if (value instanceof Boolean) {
            return "(布尔值)";
        } else {
            return "(对象)";
        }
    }

    /**
     * 检查响应是否为空数据（完全通用方法）
     */
    private boolean isEmptyDataResponse(String response) {
        try {
            if (response == null || response.trim().isEmpty()) {
                return false;
            }

            JSONObject responseObj = JSONObject.parseObject(response);
            if (responseObj == null) {
                return false;
            }

            // 通用检查：data字段是否为null，且msg为空（表示查询无结果）
            Object dataObj = responseObj.get("data");
            Object msgObj = responseObj.get("msg");
            String msg = msgObj != null ? msgObj.toString() : null;

            // 只有当data为null且msg为空时，才认为是查询无结果
            return dataObj == null && (msg == null || msg.trim().isEmpty());

        } catch (Exception e) {
            log.debug("检查空数据响应失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 构建任务指导（通用方法，不依赖特例化）
     */
    private String buildGuidance(String currentTask, Integer currentStep, String lastResponse) {
        try {
            // 优先使用LLM1自己提供的下一步指令
            Object nextStepObj = stateManager.getState("next_step");
            if (nextStepObj != null) {
                String nextStepFromLLM = nextStepObj.toString();
                if (!nextStepFromLLM.trim().isEmpty()) {
                    log.info("使用LLM1提供的下一步指令: {}", nextStepFromLLM);
                    return nextStepFromLLM;
                }
            }

            // 如果是新任务的第一步
            if (currentStep == 0 && (lastResponse == null || lastResponse.trim().isEmpty())) {
                return String.format("开始执行任务: %s，请根据任务要求和可用能力选择合适的操作", currentTask);
            }

            // 如果有上一步的响应，分析响应结果
            if (lastResponse != null && !lastResponse.trim().isEmpty()) {
                // 检查成功标识
                if (isSuccessResponse(lastResponse)) {
                    // 通用检查：如果响应成功但数据为空，强烈建议结束任务
                    if (isEmptyDataResponse(lastResponse)) {
                        return String.format("重要：任务 '%s' 已经没有更多数据可处理，请立即将任务标记为完成(yes)", currentTask);
                    }
                    // 通用的成功处理，让LLM自己判断下一步
                    return String.format("任务 '%s' 上一步操作成功，请根据获得的数据和任务要求选择下一步操作", currentTask);
                }

                // 检查错误标识
                if (isErrorResponse(lastResponse)) {
                    return String.format("任务 '%s' 上一步遇到错误，请分析错误原因并重试或调整策略", currentTask);
                }
            }

            // 默认指导
            return String.format("继续执行任务: %s，当前第%d步，请选择合适的操作", currentTask, currentStep + 1);

        } catch (Exception e) {
            log.error("构建任务指导失败: {}", e.getMessage(), e);
            return String.format("继续执行任务: %s，当前第%d步", currentTask, currentStep + 1);
        }
    }

    /**
     * 检查响应是否表示成功
     */
    private boolean isSuccessResponse(String response) {
        if (response == null) return false;

        return response.contains("成功") ||
               response.contains("success") ||
               response.contains("code\":10001") ||
               response.contains("登录成功") ||
               response.contains("发帖成功") ||
               response.contains("完成");
    }

    /**
     * 检查响应是否表示错误
     */
    private boolean isErrorResponse(String response) {
        if (response == null) return false;

        return response.contains("错误") ||
               response.contains("失败") ||
               response.contains("error") ||
               response.contains("code\":10002") ||
               response.contains("异常");
    }


    
    /**
     * 创建聊天请求
     */
    private ChatRequest createChatRequest(Prompt prompt) {
        ChatRequest request = new ChatRequest();
        request.setRequestId(String.valueOf(System.currentTimeMillis()));
        request.setUserId("digital_life");
        request.setSessionId("digital_life_session");
        request.setModel(springAIChatStarterConfig.getModel());
        request.setStream(false);
        request.setPrompt(prompt.printDescription() + prompt.toString());
        
        return request;
    }
    
    /**
     * 处理LLM响应
     */
    private boolean processLLMResponse(ChatResponse response, String currentTask) {
        try {
            log.info("LLM响应: {}", response.getContent());
            
            JSONObject responseObj = JSONObject.parseObject(response.getContent());
            
            // 提取动作指令
            Object actionInstructionObj = responseObj.get("动作指令（whatCanIDo选一个，只能选一个最佳的动作，JSON格式输出完整描述key-动作value）");
            if (actionInstructionObj == null) {
                // 尝试其他可能的字段名
                actionInstructionObj = responseObj.get("动作指令");
            }
            
            if (actionInstructionObj != null) {
                String rawActionInstruction = null;
                
                // 提取原始动作指令
                if (actionInstructionObj instanceof String) {
                    rawActionInstruction = (String) actionInstructionObj;
                } else {
                    rawActionInstruction = actionInstructionObj.toString();
                }
                
                if (rawActionInstruction != null && !rawActionInstruction.equals("null") && !rawActionInstruction.equals("")) {
                    log.info("原始动作指令: {}", rawActionInstruction);

                    // 使用智能处理：先尝试直接执行，失败后再格式化
                    boolean success = actionFormatter.smartProcessAction(rawActionInstruction, currentTask);

                    if (!success) {
                        log.warn("动作指令处理失败，跳过执行");
                    }
                }
            }
            
            // 提取其他信息（兼容多种字段名格式）
            String nextStep = responseObj.getString("下一步指令");
            if (nextStep == null) {
                nextStep = responseObj.getString("下一步指令（输出指令指导下游LLM，不要重复包括这次的动作）");
            }

            String currentStepResult = responseObj.getString("当前这一步理想执行结果");
            String isTaskDone = responseObj.getString("执行完当前这一步任务是否完成(yes/no)");
            
            log.info("当前步骤结果: {}", currentStepResult);
            log.info("下一步指令: {}", nextStep);
            log.info("任务是否完成: {}", isTaskDone);
            
            // 更新状态
            if (currentStepResult != null) {
                stateManager.saveState("current_step_result", currentStepResult);
            }
            if (nextStep != null) {
                stateManager.saveState("next_step", nextStep);
            }
            
            // 更新步骤计数
            Integer currentStep = stateManager.getCurrentStep();
            stateManager.saveTaskProgress(currentTask, currentStep + 1);
            
            // 使用专门的任务完成判断服务
            String executionHistory = taskCompletionJudge.buildExecutionHistory(currentTask, currentStep + 1, currentStepResult);
            String lastResponse = stateManager.getLastResponse();
            boolean shouldComplete = taskCompletionJudge.shouldCompleteTask(currentTask, executionHistory, lastResponse);

            log.info("专门判断服务结果: {}, 原LLM判断: {}", shouldComplete, isTaskDone);

            // 优先使用专门判断服务的结果
            return shouldComplete;
            
        } catch (Exception e) {
            log.error("处理LLM响应失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取数字生命运行状态
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取当前状态信息
     */
    public String getStatusInfo() {
        StringBuilder status = new StringBuilder();
        status.append("数字生命状态: ").append(isRunning ? "运行中" : "已停止").append("\n");
        status.append("当前任务: ").append(stateManager.getCurrentTask()).append("\n");
        status.append("当前步骤: ").append(stateManager.getCurrentStep()).append("\n");
        status.append("登录状态: ").append(stateManager.hasState("login_token") ? "已登录" : "未登录").append("\n");
        return status.toString();
    }
}
