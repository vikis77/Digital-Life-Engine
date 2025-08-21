package com.randb.digitaldemo1.entity;

import lombok.Data;
import java.util.Map;

/**
 * HTTP请求信息封装类
 * @author: randb
 * @date: 2025-08-22
 */
@Data
public class HttpRequestInfo {
    
    /**
     * HTTP方法 (GET, POST, PUT, DELETE等)
     */
    private String method;
    
    /**
     * 请求URL
     */
    private String url;
    
    /**
     * 请求参数 (查询参数)
     */
    private Map<String, Object> params;
    
    /**
     * 请求体 (JSON对象)
     */
    private Map<String, Object> body;
    
    /**
     * 请求体 (字符串格式)
     */
    private String bodyString;
    
    /**
     * 检查HTTP请求信息是否有效
     * @return 如果method和url都不为空则返回true
     */
    public boolean isValid() {
        return method != null && !method.trim().isEmpty() 
            && url != null && !url.trim().isEmpty();
    }
    
    /**
     * 获取格式化的请求信息字符串
     * @return 格式化的请求信息
     */
    public String getFormattedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP Request Info:\n");
        sb.append("  Method: ").append(method).append("\n");
        sb.append("  URL: ").append(url).append("\n");
        
        if (params != null && !params.isEmpty()) {
            sb.append("  Params: ").append(params).append("\n");
        }
        
        if (body != null && !body.isEmpty()) {
            sb.append("  Body: ").append(body).append("\n");
        } else if (bodyString != null && !bodyString.trim().isEmpty()) {
            sb.append("  Body: ").append(bodyString).append("\n");
        }
        
        return sb.toString();
    }
}
