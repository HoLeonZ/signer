package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.entity.JamendoConfig;
import com.aisinger.entity.LlmConfig;
import com.aisinger.service.JamendoConfigService;
import com.aisinger.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理Controller
 * 管理LLM和Jamendo等外部服务配置
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemConfigController {
    
    private final LlmConfigService llmConfigService;
    private final JamendoConfigService jamendoConfigService;
    
    // ==================== LLM配置 ====================
    
    /**
     * 获取所有LLM配置
     */
    @GetMapping("/llm")
    public ApiResponse<List<LlmConfig>> getAllLlmConfigs() {
        return ApiResponse.success(llmConfigService.getAllConfigs());
    }
    
    /**
     * 获取当前激活的LLM配置
     */
    @GetMapping("/llm/active")
    public ApiResponse<LlmConfig> getActiveLlmConfig() {
        return ApiResponse.success(llmConfigService.getActiveConfig());
    }
    
    /**
     * 获取单个LLM配置
     */
    @GetMapping("/llm/{id}")
    public ApiResponse<LlmConfig> getLlmConfigById(@PathVariable Long id) {
        return llmConfigService.getConfigById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("配置不存在"));
    }
    
    /**
     * 创建LLM配置
     */
    @PostMapping("/llm")
    public ApiResponse<LlmConfig> createLlmConfig(@RequestBody LlmConfig config) {
        try {
            LlmConfig created = llmConfigService.createConfig(config);
            return ApiResponse.success("LLM配置创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新LLM配置
     */
    @PutMapping("/llm/{id}")
    public ApiResponse<LlmConfig> updateLlmConfig(@PathVariable Long id, @RequestBody LlmConfig config) {
        try {
            LlmConfig updated = llmConfigService.updateConfig(id, config);
            return ApiResponse.success("LLM配置更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 设置激活的LLM提供商
     */
    @PostMapping("/llm/{id}/activate")
    public ApiResponse<LlmConfig> activateLlmConfig(@PathVariable Long id) {
        try {
            LlmConfig activated = llmConfigService.setActiveProvider(id);
            return ApiResponse.success("已切换到: " + activated.getDisplayName(), activated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除LLM配置
     */
    @DeleteMapping("/llm/{id}")
    public ApiResponse<Void> deleteLlmConfig(@PathVariable Long id) {
        try {
            llmConfigService.deleteConfig(id);
            return ApiResponse.success("LLM配置删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 测试LLM连接
     */
    @PostMapping("/llm/{id}/test")
    public ApiResponse<Map<String, Object>> testLlmConnection(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = llmConfigService.testConnection(id);
        result.put("success", success);
        result.put("message", success ? "连接成功" : "连接失败");
        return ApiResponse.success(result);
    }
    
    // ==================== Jamendo配置 ====================
    
    /**
     * 获取Jamendo配置
     */
    @GetMapping("/jamendo")
    public ApiResponse<JamendoConfig> getJamendoConfig() {
        return ApiResponse.success(jamendoConfigService.getConfig());
    }
    
    /**
     * 保存Jamendo配置
     */
    @PostMapping("/jamendo")
    public ApiResponse<JamendoConfig> saveJamendoConfig(@RequestBody JamendoConfig config) {
        try {
            if (config.getName() == null || config.getName().isEmpty()) {
                config.setName("default");
            }
            JamendoConfig saved = jamendoConfigService.saveConfig(config);
            return ApiResponse.success("Jamendo配置保存成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新Jamendo配置
     */
    @PutMapping("/jamendo/{id}")
    public ApiResponse<JamendoConfig> updateJamendoConfig(@PathVariable Long id, @RequestBody JamendoConfig config) {
        try {
            config.setName("default");
            JamendoConfig saved = jamendoConfigService.saveConfig(config);
            return ApiResponse.success("Jamendo配置更新成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 测试Jamendo连接
     */
    @PostMapping("/jamendo/test")
    public ApiResponse<Map<String, Object>> testJamendoConnection() {
        Map<String, Object> result = new HashMap<>();
        boolean success = jamendoConfigService.testConnection();
        result.put("success", success);
        result.put("message", success ? "配置有效" : "Client ID未配置");
        return ApiResponse.success(result);
    }
    
    // ==================== 综合状态 ====================
    
    /**
     * 获取系统配置状态概览
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getConfigStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // LLM状态
        LlmConfig activeLlm = llmConfigService.getActiveConfig();
        Map<String, Object> llmStatus = new HashMap<>();
        llmStatus.put("provider", activeLlm.getProvider());
        llmStatus.put("displayName", activeLlm.getDisplayName());
        llmStatus.put("hasApiKey", activeLlm.getApiKey() != null && !activeLlm.getApiKey().isEmpty());
        llmStatus.put("enabled", activeLlm.getEnabled());
        status.put("llm", llmStatus);
        
        // Jamendo状态
        JamendoConfig jamendoConfig = jamendoConfigService.getConfig();
        Map<String, Object> jamendoStatus = new HashMap<>();
        jamendoStatus.put("enabled", jamendoConfig.getEnabled());
        jamendoStatus.put("hasClientId", jamendoConfig.getClientId() != null && !jamendoConfig.getClientId().isEmpty());
        status.put("jamendo", jamendoStatus);
        
        return ApiResponse.success(status);
    }
}
