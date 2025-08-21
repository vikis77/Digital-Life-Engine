package com.randb.digitaldemo1.controller;

import com.randb.digitaldemo1.service.ActionFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ActionFormatter测试控制器
 * @author: randb
 * @date: 2025-08-22
 */
@RestController
@RequestMapping("/api/formatter-test")
@Slf4j
public class ActionFormatterTestController {

    @Autowired
    private ActionFormatter actionFormatter;

    /**
     * 测试动作格式化
     */
    @PostMapping("/format")
    public String testFormat(@RequestParam String task, @RequestBody String rawAction) {
        try {
            log.info("测试格式化 - 任务: {}, 原始动作: {}", task, rawAction);
            
            String formatted = actionFormatter.formatAction(rawAction, task);
            
            if (formatted != null) {
                return "格式化成功:\n" + formatted;
            } else {
                return "格式化失败";
            }
        } catch (Exception e) {
            log.error("测试格式化失败: {}", e.getMessage(), e);
            return "测试失败: " + e.getMessage();
        }
    }

    /**
     * 测试把大象放进冰箱任务的格式化
     */
    @PostMapping("/test-elephant")
    public String testElephantTask() {
        String rawAction = """
            {
                "动作指令（whatCanIDo选一个，只能选一个最佳的动作，JSON格式输出完整描述key-动作value）": "{\\"描述\\":\\"打开冰箱门\\",\\"动作\\":{\\"url\\":\\"http://localhost:8080/api/test/opendoor\\",\\"method\\":\\"POST\\",\\"params\\":{},\\"body\\":\\"\\"}}",
                "下一步指令（输出指令指导下游LLM，不要重复包括这次的动作）": "执行完打开冰箱门后，下一步是放大象进冰箱。",
                "当前这一步理想执行结果": "冰箱门成功打开。",
                "执行完当前这一步任务是否完成(yes/no)": "no"
            }
            """;
        
        return testFormat("把大象放进冰箱", rawAction);
    }

    /**
     * 测试登录任务的格式化
     */
    @PostMapping("/test-login")
    public String testLoginTask() {
        String rawAction = """
            {
                "动作指令": {
                    "任务": "登录校猫日记平台",
                    "步骤": [
                        {
                            "描述": "登录",
                            "动作": {
                                "url": "http://localhost:8080/api/user/login",
                                "method": "POST",
                                "params": {},
                                "body": {
                                    "username": "robot1",
                                    "password": "a1111111"
                                }
                            }
                        }
                    ]
                },
                "下一步指令": "登录操作已完成，现在可以尝试发布一个帖子或执行其他任务。",
                "当前这一步理想执行结果": "成功登录校猫日记平台并获得认证token。",
                "执行完当前这一步任务是否完成(yes/no)": "no"
            }
            """;
        
        return testFormat("登录校猫日记平台", rawAction);
    }

    /**
     * 检查格式化需求
     */
    @PostMapping("/check-needs-formatting")
    public String checkNeedsFormatting(@RequestBody String actionInstruction) {
        try {
            boolean needs = actionFormatter.needsFormatting(actionInstruction);
            return "需要格式化: " + needs;
        } catch (Exception e) {
            return "检查失败: " + e.getMessage();
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        return "ActionFormatter测试服务正常运行";
    }
}
