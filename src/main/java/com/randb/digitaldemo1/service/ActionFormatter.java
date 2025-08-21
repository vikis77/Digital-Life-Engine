package com.randb.digitaldemo1.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.randb.digitaldemo1.config.SpringAIChatStarterConfig;
import com.randb.springaichatstarter.core.ChatModelFactory;
import com.randb.springaichatstarter.core.ChatService;
import com.randb.springaichatstarter.dto.ChatRequest;
import com.randb.springaichatstarter.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 动作格式化器
 * 使用第二个LLM将第一个LLM的输出转换为标准格式
 * @author: randb
 * @date: 2025-08-22
 */
@Component
@Slf4j
public class ActionFormatter {

    @Autowired
    private ChatModelFactory chatModelFactory;

    @Autowired
    private StateManager stateManager;

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private SpringAIChatStarterConfig springAIChatStarterConfig;

    /**
     * 智能处理动作指令：先尝试直接执行，失败后再格式化
     * @param llm1Output LLM1的原始输出
     * @param currentTask 当前任务名称
     * @return 处理结果：true表示成功执行，false表示需要进一步处理
     */
    public boolean smartProcessAction(String llm1Output, String currentTask) {
        try {
            log.info("🧠 智能处理动作指令开始...");

            // 1. 先尝试直接解析并执行原始指令
            if (tryDirectExecution(llm1Output, currentTask)) {
                log.info("✅ 直接执行成功，无需格式化");
                return true;
            }

            // 2. 直接执行失败，尝试格式化后执行
            log.info("🔄 直接执行失败，开始格式化...");
            String formattedAction = formatActionInternal(llm1Output, currentTask);

            if (formattedAction != null) {
                // 执行格式化后的动作
                com.randb.digitaldemo1.core.ActionExecutor.executeComplexAction(formattedAction);
                log.info("✅ 格式化后执行成功");
                return true;
            } else {
                log.warn("⚠️ 格式化失败，无法执行动作");
                return false;
            }

        } catch (Exception e) {
            log.error("❌ 智能处理动作指令失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 尝试直接执行原始指令
     */
    private boolean tryDirectExecution(String llm1Output, String currentTask) {
        try {
            log.info("尝试直接执行原始指令: {}", llm1Output);

            // 检查是否已经是标准格式
            if (isStandardFormat(llm1Output)) {
                log.info("✅ 识别为标准格式，直接执行");
                com.randb.digitaldemo1.core.ActionExecutor.executeComplexAction(llm1Output);
                return true;
            }

            // 尝试简单的JSON解析和执行
            JSONObject json = JSONObject.parseObject(llm1Output);
            log.info("📋 解析JSON成功，检查动作指令字段");

            // 检查"动作指令"字段（新的LLM1输出格式）
            if (json.containsKey("动作指令")) {
                Object actionObj = json.get("动作指令");
                log.info("找到动作指令字段，类型: {}", actionObj.getClass().getSimpleName());

                // 如果是完整的动作配置对象
                if (actionObj instanceof JSONObject) {
                    JSONObject actionConfig = (JSONObject) actionObj;
                    log.info("📝 动作配置内容: {}", actionConfig.toJSONString());

                    if (actionConfig.containsKey("url") && actionConfig.containsKey("method")) {
                        log.info("🎯 发现完整的动作配置（动作指令字段），直接执行");
                        return executeActionFromConfig(actionConfig, currentTask);
                    } else {
                        log.warn("⚠️ 动作配置缺少url或method字段");
                    }
                }

                // 如果是动作描述字符串，尝试从ability.txt匹配
                if (actionObj instanceof String) {
                    String actionDescription = (String) actionObj;
                    log.info("尝试从ability.txt匹配动作: {}", actionDescription);
                    return executeSimpleAction(actionDescription, currentTask);
                }
            }

            // 检查"whatCanIDo"字段（旧的格式兼容）
            if (json.containsKey("whatCanIDo")) {
                Object actionObj = json.get("whatCanIDo");
                log.info("找到whatCanIDo字段，类型: {}", actionObj.getClass().getSimpleName());

                // 如果是完整的动作配置对象
                if (actionObj instanceof JSONObject) {
                    JSONObject actionConfig = (JSONObject) actionObj;
                    if (actionConfig.containsKey("url") && actionConfig.containsKey("method")) {
                        log.info("🎯 发现完整的动作配置（whatCanIDo字段），直接执行");
                        return executeActionFromConfig(actionConfig, currentTask);
                    }
                }

                // 如果是动作描述字符串，尝试从ability.txt匹配
                if (actionObj instanceof String) {
                    String actionDescription = (String) actionObj;
                    log.info("尝试从ability.txt匹配动作: {}", actionDescription);
                    return executeSimpleAction(actionDescription, currentTask);
                }
            }

            // 检查是否直接是HTTP配置（顶级字段包含url和method）
            if (json.containsKey("url") && json.containsKey("method")) {
                log.info("🎯 发现顶级HTTP配置，直接执行");
                return executeActionFromConfig(json, currentTask);
            }

            log.warn("⚠️ 未找到可识别的动作指令格式");
            return false;
        } catch (Exception e) {
            log.error("❌ 直接执行失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行完整的动作配置
     */
    private boolean executeActionFromConfig(JSONObject actionConfig, String currentTask) {
        try {
            String method = actionConfig.getString("method");
            String url = actionConfig.getString("url");
            JSONObject params = actionConfig.getJSONObject("params");
            Object body = actionConfig.get("body");

            // 直接发送HTTP请求（自动添加token）
            ResponseEntity<String> response = sendHttpRequestWithToken(url, method, params, body);

            // 检查响应是否成功
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("执行动作配置失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 执行简单动作（通用化实现，基于ability.txt配置）
     */
    private boolean executeSimpleAction(String actionDescription, String currentTask) {
        try {
            // 从ability.txt中查找匹配的动作
            String abilityContent = readAbilityFile();
            if (abilityContent == null) {
                return false;
            }

            // 解析ability.txt，查找当前任务的动作配置
            JSONObject abilityJson = JSONObject.parseObject(abilityContent);
            JSONArray tasks = abilityJson.getJSONArray("tasks");

            for (int i = 0; i < tasks.size(); i++) {
                JSONObject task = tasks.getJSONObject(i);
                String taskName = task.getString("任务");

                // 找到匹配的任务
                if (currentTask.equals(taskName)) {
                    JSONArray steps = task.getJSONArray("步骤");

                    // 在步骤中查找匹配的动作描述
                    for (int j = 0; j < steps.size(); j++) {
                        JSONObject step = steps.getJSONObject(j);
                        String stepDescription = step.getString("描述");

                        // 使用模糊匹配来判断动作描述是否匹配
                        if (isActionMatch(actionDescription, stepDescription)) {
                            JSONObject action = step.getJSONObject("动作");
                            if (action != null && action.containsKey("url")) {
                                return executeActionFromAbility(action);
                            }
                        }
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.debug("简单动作执行失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 读取ability.txt文件
     */
    private String readAbilityFile() {
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get("src/main/resources/ability.txt"));
        } catch (Exception e) {
            log.debug("读取ability.txt失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断动作描述是否匹配（模糊匹配）
     */
    private boolean isActionMatch(String actionDescription, String stepDescription) {
        if (actionDescription == null || stepDescription == null) {
            return false;
        }

        // 提取关键词进行匹配
        String[] actionKeywords = extractKeywords(actionDescription);
        String[] stepKeywords = extractKeywords(stepDescription);

        // 计算匹配度
        int matchCount = 0;
        for (String actionKeyword : actionKeywords) {
            for (String stepKeyword : stepKeywords) {
                if (actionKeyword.equals(stepKeyword)) {
                    matchCount++;
                    break;
                }
            }
        }

        // 如果有50%以上的关键词匹配，认为是匹配的
        return matchCount > 0 && (double) matchCount / actionKeywords.length >= 0.5;
    }

    /**
     * 提取关键词
     */
    private String[] extractKeywords(String text) {
        // 简单的关键词提取，去除常见的停用词
        String[] stopWords = {"的", "了", "在", "是", "和", "与", "或", "但", "然后", "接着", "之后"};

        String[] words = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", " ").split("\\s+");
        java.util.List<String> keywords = new java.util.ArrayList<>();

        for (String word : words) {
            if (word.length() > 1 && !java.util.Arrays.asList(stopWords).contains(word)) {
                keywords.add(word);
            }
        }

        return keywords.toArray(new String[0]);
    }

    /**
     * 执行从ability.txt中解析出的动作
     */
    private boolean executeActionFromAbility(JSONObject action) {
        try {
            String method = action.getString("method");
            String url = action.getString("url");
            JSONObject params = action.getJSONObject("params");
            Object body = action.get("body");

            // 直接发送HTTP请求（自动添加token）
            ResponseEntity<String> response = sendHttpRequestWithToken(url, method, params, body);

            // 检查响应是否成功
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("执行ability动作失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构造标准格式的动作指令
     */
    private String buildStandardAction(String method, String url, JSONObject params, Object body) {
        return buildStandardAction(method, url, params, body, "通用动作");
    }

    /**
     * 构造标准格式的动作指令（带任务名称）
     */
    private String buildStandardAction(String method, String url, JSONObject params, Object body, String taskName) {
        JSONObject action = new JSONObject();
        action.put("method", method);
        action.put("url", url);
        action.put("params", params != null ? params : new JSONObject());
        action.put("body", body != null ? body : new JSONObject());

        JSONObject step = new JSONObject();
        step.put("描述", "执行动作");
        step.put("动作", action);

        JSONArray steps = new JSONArray();
        steps.add(step);

        JSONObject task = new JSONObject();
        task.put("步骤", steps);

        JSONObject result = new JSONObject();
        result.put(taskName, task);

        return result.toJSONString();
    }

    /**
     * 发送HTTP请求（自动添加Bearer token）
     */
    private ResponseEntity<String> sendHttpRequestWithToken(String url, String method, JSONObject params, Object body) {
        try {
            log.info("🌐 发送HTTP请求: {} {}", method, url);
            log.info("📝 请求参数: {}", params);
            log.info("📦 请求体: {}", body);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 自动添加Bearer token（如果存在）
            if (stateManager != null && stateManager.hasState("login_token")) {
                String token = stateManager.getLoginToken();
                headers.setBearerAuth(token);
                log.info("🔐 自动添加认证token到请求头");
            }

            // 准备请求体
            Object requestBody = null;
            if (body != null) {
                if (body instanceof JSONObject || body instanceof java.util.Map) {
                    requestBody = body;
                } else if (body instanceof String && !((String) body).trim().isEmpty()) {
                    requestBody = body;
                }
            }

            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);

            log.info("✅ HTTP请求响应状态: {}", response.getStatusCode());
            log.info("📄 HTTP请求响应内容: {}", response.getBody());

            // 保存响应到状态管理器
            if (stateManager != null) {
                stateManager.saveLastResponse(response.getBody());

                // 如果是登录请求，尝试提取token
                if (url.contains("login") && response.getStatusCode().is2xxSuccessful()) {
                    extractAndSaveToken(response.getBody());
                }
            }

            return response;

        } catch (Exception e) {
            log.error("❌ 发送HTTP请求失败: {}", e.getMessage(), e);
            if (stateManager != null) {
                stateManager.saveLastResponse("ERROR: " + e.getMessage());
            }
            throw new RuntimeException("HTTP请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从登录响应中提取并保存token
     */
    private void extractAndSaveToken(String responseBody) {
        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                JSONObject responseNode = JSONObject.parseObject(responseBody);

                // 首先检查是否有data字段（嵌套结构）
                if (responseNode.containsKey("data")) {
                    JSONObject dataNode = responseNode.getJSONObject("data");
                    if (dataNode.containsKey("token")) {
                        String token = dataNode.getString("token");
                        if (token != null && !token.isEmpty()) {
                            stateManager.saveLoginToken(token);
                            log.info("✅ 成功提取并保存登录token");
                            return;
                        }
                    }
                }

                // 尝试多种可能的顶级token字段名
                String[] tokenFields = {"token", "accessToken", "access_token", "authToken", "jwt"};

                for (String field : tokenFields) {
                    if (responseNode.containsKey(field)) {
                        String token = responseNode.getString(field);
                        if (token != null && !token.isEmpty()) {
                            stateManager.saveLoginToken(token);
                            log.info("✅ 成功提取并保存登录token");
                            return;
                        }
                    }
                }

                log.warn("⚠️ 未能从登录响应中提取token");
            }
        } catch (Exception e) {
            log.error("❌ 提取token失败: {}", e.getMessage(), e);
        }
    }



    /**
     * 检查是否为标准格式
     */
    private boolean isStandardFormat(String actionInstruction) {
        try {
            JSONObject json = JSONObject.parseObject(actionInstruction);
            // 检查是否包含标准格式的关键字段
            for (String key : json.keySet()) {
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    JSONObject valueObj = (JSONObject) value;
                    if (valueObj.containsKey("步骤")) {
                        return true; // 已经是标准格式
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 内部格式化方法（原来的formatAction逻辑）
     */
    private String formatActionInternal(String llm1Output, String currentTask) {
        try {
            // 构建格式化prompt
            String formatPrompt = buildFormatPrompt(llm1Output, currentTask);

            // 调用LLM2进行格式化
            ChatService chatService = chatModelFactory.get(springAIChatStarterConfig.getModel());
            ChatRequest request = createFormatRequest(formatPrompt);
            ChatResponse response = chatService.syncReply(request);

            String formattedAction = extractFormattedAction(response.getContent());

            log.info("✅ 动作指令格式化完成");
            return formattedAction;

        } catch (Exception e) {
            log.error("❌ 动作格式化失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保持原有的formatAction方法以兼容现有代码
     */
    public String formatAction(String llm1Output, String currentTask) {
        return formatActionInternal(llm1Output, currentTask);
    }

    /**
     * 构建格式化prompt
     */
    private String buildFormatPrompt(String llm1Output, String currentTask) {
        return String.format("""
            你是一个专门的JSON格式转换器。请将以下LLM输出转换为标准的动作执行格式。

            当前任务：%s

            LLM原始输出：
            %s

            重要原则：
            1. 只转换LLM指定的单个动作，不要添加额外的步骤
            2. 如果LLM只指定了一个API调用，就只生成一个步骤
            3. 保持LLM的原始意图，不要扩展为完整流程

            请严格按照以下格式输出JSON（不要添加任何其他文字）：
            {
              "%s": {
                "步骤": [
                  {
                    "描述": "执行LLM指定的动作",
                    "动作": {
                      "method": "从LLM输出中提取的method",
                      "url": "从LLM输出中提取的url",
                      "params": {},
                      "body": {}
                    }
                  }
                ]
              }
            }

            示例：
            - 如果LLM输出包含opendoor的URL，就只生成打开冰箱门的步骤
            - 如果LLM输出包含login的URL，就只生成登录步骤
            - 如果LLM输出包含send/post的URL，就只生成发帖步骤

            任务名称必须是：%s
            只输出JSON，不要其他内容！
            """, currentTask, llm1Output, currentTask, currentTask);
    }

    /**
     * 创建格式化请求
     */
    private ChatRequest createFormatRequest(String prompt) {
        ChatRequest request = new ChatRequest();
        request.setRequestId("format_" + System.currentTimeMillis());
        request.setUserId("action_formatter");
        request.setSessionId("format_session");
        request.setModel(springAIChatStarterConfig.getModel());
        request.setStream(false);
        request.setPrompt(prompt);
        return request;
    }

    /**
     * 从格式化响应中提取JSON
     */
    private String extractFormattedAction(String response) {
        try {
            // 查找JSON部分
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}");
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonStr = response.substring(startIndex, endIndex + 1);
                
                // 验证JSON格式
                JSONObject.parseObject(jsonStr);
                
                return jsonStr;
            } else {
                log.warn("⚠️ 无法从格式化响应中提取JSON: {}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ 提取格式化JSON失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查是否需要格式化
     * @param actionInstruction 动作指令
     * @return 是否需要格式化
     */
    public boolean needsFormatting(String actionInstruction) {
        try {
            if (actionInstruction == null || actionInstruction.trim().isEmpty()) {
                return false;
            }
            
            // 尝试解析为JSON
            JSONObject json = JSONObject.parseObject(actionInstruction);
            
            // 检查是否包含标准格式的关键字段
            for (String key : json.keySet()) {
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    JSONObject valueObj = (JSONObject) value;
                    if (valueObj.containsKey("步骤")) {
                        return false; // 已经是标准格式
                    }
                }
            }
            
            return true; // 需要格式化
        } catch (Exception e) {
            return true; // 解析失败，需要格式化
        }
    }
}
