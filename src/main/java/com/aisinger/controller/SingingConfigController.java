package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.entity.SingingConfig;
import com.aisinger.service.SingingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 演唱配置管理Controller
 */
@RestController
@RequestMapping("/api/singing-configs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SingingConfigController {
    
    private final SingingConfigService singingConfigService;
    
    /**
     * 获取所有启用的配置
     */
    @GetMapping
    public ApiResponse<List<SingingConfig>> getAllConfigs() {
        return ApiResponse.success(singingConfigService.getAllConfigs());
    }
    
    /**
     * 获取所有配置（包括禁用的）
     */
    @GetMapping("/all")
    public ApiResponse<List<SingingConfig>> getAllConfigsIncludeDisabled() {
        return ApiResponse.success(singingConfigService.getAllConfigsIncludeDisabled());
    }
    
    /**
     * 根据ID获取配置
     */
    @GetMapping("/{id}")
    public ApiResponse<SingingConfig> getConfigById(@PathVariable Long id) {
        return singingConfigService.getConfigById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("配置不存在"));
    }
    
    /**
     * 根据分类获取配置
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<SingingConfig>> getConfigsByCategory(@PathVariable String category) {
        return ApiResponse.success(singingConfigService.getConfigsByCategory(category));
    }
    
    /**
     * 获取系统预设
     */
    @GetMapping("/presets")
    public ApiResponse<List<SingingConfig>> getPresets() {
        return ApiResponse.success(singingConfigService.getPresets());
    }
    
    /**
     * 获取用户自定义配置
     */
    @GetMapping("/custom")
    public ApiResponse<List<SingingConfig>> getCustomConfigs() {
        return ApiResponse.success(singingConfigService.getCustomConfigs());
    }
    
    /**
     * 获取热门配置
     */
    @GetMapping("/popular")
    public ApiResponse<List<SingingConfig>> getPopularConfigs() {
        return ApiResponse.success(singingConfigService.getPopularConfigs());
    }
    
    /**
     * 搜索配置
     */
    @GetMapping("/search")
    public ApiResponse<List<SingingConfig>> searchConfigs(@RequestParam String keyword) {
        return ApiResponse.success(singingConfigService.searchConfigs(keyword));
    }
    
    /**
     * 创建配置
     */
    @PostMapping
    public ApiResponse<SingingConfig> createConfig(@RequestBody SingingConfig config) {
        SingingConfig created = singingConfigService.createConfig(config);
        return ApiResponse.success("演唱配置创建成功", created);
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public ApiResponse<SingingConfig> updateConfig(@PathVariable Long id, @RequestBody SingingConfig config) {
        try {
            SingingConfig updated = singingConfigService.updateConfig(id, config);
            return ApiResponse.success("演唱配置更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        singingConfigService.deleteConfig(id);
        return ApiResponse.success("演唱配置删除成功", null);
    }
    
    /**
     * 复制配置
     */
    @PostMapping("/{id}/duplicate")
    public ApiResponse<SingingConfig> duplicateConfig(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        String newName = params != null ? params.get("name") : null;
        SingingConfig duplicated = singingConfigService.duplicateConfig(id, newName);
        return ApiResponse.success("配置复制成功", duplicated);
    }
    
    /**
     * 使用配置（增加使用计数）
     */
    @PostMapping("/{id}/use")
    public ApiResponse<Void> useConfig(@PathVariable Long id) {
        singingConfigService.incrementUseCount(id);
        return ApiResponse.success("已记录使用", null);
    }
}
