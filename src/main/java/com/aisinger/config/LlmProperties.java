package com.aisinger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    
    /**
     * 当前启用的LLM提供商: qwen, openai, gemini
     */
    private String provider = "qwen";
    
    /**
     * 通义千问配置
     */
    private ProviderConfig qwen = new ProviderConfig();
    
    /**
     * OpenAI配置
     */
    private ProviderConfig openai = new ProviderConfig();
    
    /**
     * Gemini配置
     */
    private ProviderConfig gemini = new ProviderConfig();
    
    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String apiUrl;
        private String model;
    }
    
    /**
     * 获取当前启用的提供商配置
     */
    public ProviderConfig getActiveProvider() {
        return switch (provider.toLowerCase()) {
            case "openai" -> openai;
            case "gemini" -> gemini;
            default -> qwen;
        };
    }
}
