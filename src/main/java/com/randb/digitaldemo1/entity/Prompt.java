package com.randb.digitaldemo1.entity;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import lombok.Data;

/**
 * @author: randb
 * @date: 2025-08-22
 */

@Data
public class Prompt {
    /**
     * 我是谁
     */
    private static String whoAmI = "我是一个数字生命。我生存在“校猫日记”这个虚拟世界平台里面，这个平台是围绕校园流浪猫记录和交友展开的。";
    /**
     * 我的任务是什么（task）
     */
    private String whatIsMytask;
    /**
     * 我刚刚做了什么（上一步的结果）
     */
    private String whatDidIJustDo;
    /**
     * 我现在要做什么（下一步的指导）
     */
    private String whatNow;
    /**
     * 我能做什么（能力）
     */
    private static String whatCanIDo;

    static {
        try {
            whatCanIDo = readFileAsString("src/main/resources/ability.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFileAsString(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] data = fis.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    public String printDescription() {
        return "你需要严格按照以下JSON格式输出，不要添加任何其他内容：\n" +
                "{\n" +
                "  \"动作指令\": {\n" +
                "    \"url\": \"完整的API地址\",\n" +
                "    \"method\": \"HTTP方法(GET/POST/PUT/DELETE)\",\n" +
                "    \"params\": {},\n" +
                "    \"body\": {}\n" +
                "  },\n" +
                "  \"下一步指令\": \"下一步的指导说明\",\n" +
                "  \"当前这一步理想执行结果\": \"当前步骤的预期结果\",\n" +
                "  \"执行完当前这一步任务是否完成(yes/no)\": \"yes或no\"\n" +
                "}\n\n" +
                "重要提示：\n" +
                "1. 每次只执行一个动作，发送一个HTTP请求\n" +
                "2. 动作指令必须包含完整的HTTP请求信息：url、method、params、body\n" +
                "3. 从whatCanIDo中找到对应的API配置，直接使用其url和method\n" +
                "4. 如果需要请求体参数，请在body中填写具体的参数值，不要使用占位符\n" +
                "5. 优先输出完整可执行的HTTP配置，避免只输出动作描述\n" +
                "5. 内容生成规则：\n" +
                "   - 遇到\"自己生成\"、\"自动生成\"等提示时，请生成具体的真实内容\n" +
                "6. 单步执行原则：\n" +
                "   - 对于多步骤任务，每次只执行一个步骤\n" +
                "   - 根据当前进度选择下一个合适的步骤\n" +
                "   - 不要一次性规划所有步骤\n" +
                "7. 基于数据的智能决策：\n" +
                "   - 仔细分析上一步的执行结果和获得的数据\n" +
                "   - 如果获得了数据，应该基于这些数据的具体内容进行下一步操作\n" +
                "   - 特别注意：严格按照字段名称使用数据，不要混淆不同字段\n" +
                "   - 不要重复获取相同的数据，要利用已有数据进行操作\n" +
                "   - 根据数据内容和任务要求，智能选择最合适的下一步动作\n" +
                "8. 任务完成判断：\n" +
                "   - 如果当前动作能够完成整个任务，请标记为\"yes\"\n" +
                "   - 如果还需要后续步骤，请标记为\"no\"\n" +
                "   - 特别重要：如果响应成功但data为null，说明没有更多数据，应该标记为\"yes\"\n" +
                "   - 根据任务性质和已完成的操作，合理判断任务是否完成\n" +
                "7. 示例格式：\n" +
                "   登录: {\"url\": \"http://localhost:8080/api/user/login\", \"method\": \"POST\", \"body\": {\"username\": \"robot1\", \"password\": \"a1111111\"}}\n" +
                "   发帖: {\"url\": \"http://localhost:8080/api/test/send/post\", \"method\": \"POST\", \"body\": {\"title\": \"数字生命的思考\", \"content\": \"今天我学会了如何更好地理解人类的需求\", \"tag\": \"数字生命\"}}\n\n";
    }

    @Override
    public String toString() {
        return "我是谁：" + whoAmI + "\n" +
                "我的任务是什么：" + whatIsMytask + "\n" +
                "我刚刚做了什么：" + whatDidIJustDo + "\n" +
                "我现在要做什么：" + whatNow + "\n" +
                "我能做什么：" + whatCanIDo + "\n";
    }

    public void init() {
        this.whatIsMytask = null;
        this.whatDidIJustDo = null;
        this.whatNow = null;
    }
}
