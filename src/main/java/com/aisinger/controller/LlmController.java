package com.aisinger.controller;

import com.aisinger.config.LlmProperties;
import com.aisinger.dto.ApiResponse;
import com.aisinger.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LLM配置控制器
 */
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LlmController {
    
    private final LlmProperties llmProperties;
    private final LlmService llmService;
    
    /**
     * 获取当前LLM配置信息
     */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getLlmConfig() {
        return ApiResponse.success(Map.of(
            "currentProvider", llmProperties.getProvider(),
            "availableProviders", List.of("qwen", "openai", "gemini"),
            "providerInfo", Map.of(
                "qwen", Map.of(
                    "name", "阿里通义千问",
                    "model", llmProperties.getQwen().getModel(),
                    "configured", isConfigured(llmProperties.getQwen().getApiKey())
                ),
                "openai", Map.of(
                    "name", "OpenAI",
                    "model", llmProperties.getOpenai().getModel(),
                    "configured", isConfigured(llmProperties.getOpenai().getApiKey())
                ),
                "gemini", Map.of(
                    "name", "Google Gemini",
                    "model", llmProperties.getGemini().getModel(),
                    "configured", isConfigured(llmProperties.getGemini().getApiKey())
                )
            )
        ));
    }
    
    /**
     * 切换LLM提供商
     */
    @PostMapping("/switch/{provider}")
    public ApiResponse<String> switchProvider(@PathVariable String provider) {
        String normalizedProvider = provider.toLowerCase();
        if (!List.of("qwen", "openai", "gemini").contains(normalizedProvider)) {
            return ApiResponse.error("不支持的LLM提供商: " + provider + "。可选: qwen, openai, gemini");
        }
        
        llmProperties.setProvider(normalizedProvider);
        return ApiResponse.success("LLM提供商已切换为: " + normalizedProvider, normalizedProvider);
    }
    
    /**
     * 获取当前使用的提供商
     */
    @GetMapping("/current")
    public ApiResponse<String> getCurrentProvider() {
        return ApiResponse.success(llmService.getCurrentProvider());
    }
    
    private boolean isConfigured(String apiKey) {
        return apiKey != null && 
               !apiKey.isEmpty() && 
               !apiKey.startsWith("your-") &&
               !apiKey.contains("api-key");
    }
}
