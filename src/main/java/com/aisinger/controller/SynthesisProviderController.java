package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.entity.SynthesisProviderConfig;
import com.aisinger.service.SynthesisProviderConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 语音合成服务配置控制器
 */
@RestController
@RequestMapping("/api/synthesis-providers")
@RequiredArgsConstructor
public class SynthesisProviderController {
    
    private final SynthesisProviderConfigService service;
    
    /**
     * 获取所有语音合成服务配置
     */
    @GetMapping
    public ApiResponse<List<SynthesisProviderConfig>> getAllConfigs() {
        return ApiResponse.success(service.getAllConfigs());
    }
    
    /**
     * 获取已启用的服务配置
     */
    @GetMapping("/enabled")
    public ApiResponse<List<SynthesisProviderConfig>> getEnabledConfigs() {
        return ApiResponse.success(service.getEnabledConfigs());
    }
    
    /**
     * 按服务类型获取配置
     */
    @GetMapping("/type/{serviceType}")
    public ApiResponse<List<SynthesisProviderConfig>> getConfigsByType(@PathVariable String serviceType) {
        return ApiResponse.success(service.getConfigsByServiceType(serviceType));
    }
    
    /**
     * 获取当前激活的服务
     */
    @GetMapping("/active")
    public ApiResponse<SynthesisProviderConfig> getActiveConfig() {
        return service.getActiveConfig()
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("未配置激活的语音合成服务"));
    }
    
    /**
     * 创建新配置
     */
    @PostMapping
    public ApiResponse<SynthesisProviderConfig> createConfig(@RequestBody SynthesisProviderConfig config) {
        try {
            return ApiResponse.success(service.saveConfig(config));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public ApiResponse<SynthesisProviderConfig> updateConfig(
            @PathVariable Long id, 
            @RequestBody SynthesisProviderConfig config) {
        try {
            return ApiResponse.success(service.updateConfig(id, config));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 切换激活的服务
     */
    @PostMapping("/switch/{provider}")
    public ApiResponse<String> switchProvider(@PathVariable String provider) {
        try {
            service.switchActiveProvider(provider);
            return ApiResponse.success("已切换到: " + provider);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteConfig(@PathVariable Long id) {
        try {
            service.deleteConfig(id);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 测试连接
     */
    @PostMapping("/{id}/test")
    public ApiResponse<SynthesisProviderConfig> testConnection(@PathVariable Long id) {
        try {
            return ApiResponse.success(service.testConnection(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
