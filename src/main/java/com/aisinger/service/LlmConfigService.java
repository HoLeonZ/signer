package com.aisinger.service;

import com.aisinger.config.LlmProperties;
import com.aisinger.entity.LlmConfig;
import com.aisinger.repository.LlmConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * LLM配置服务
 * 数据库配置优先于YAML配置
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LlmConfigService {
    
    private final LlmConfigRepository llmConfigRepository;
    private final LlmProperties llmProperties; // YAML兜底配置
    
    /**
     * 获取所有LLM配置
     */
    public List<LlmConfig> getAllConfigs() {
        return llmConfigRepository.findAllByOrderBySortOrderAsc();
    }
    
    /**
     * 获取所有启用的LLM配置
     */
    public List<LlmConfig> getEnabledConfigs() {
        return llmConfigRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    /**
     * 根据ID获取配置
     */
    public Optional<LlmConfig> getConfigById(Long id) {
        return llmConfigRepository.findById(id);
    }
    
    /**
     * 根据提供商获取配置
     */
    public Optional<LlmConfig> getConfigByProvider(String provider) {
        return llmConfigRepository.findByProvider(provider);
    }
    
    /**
     * 获取当前激活的LLM配置
     * 优先级：数据库激活配置 > YAML配置
     */
    public LlmConfig getActiveConfig() {
        // 1. 尝试从数据库获取激活配置
        Optional<LlmConfig> dbConfig = llmConfigRepository.findByIsActiveTrue();
        if (dbConfig.isPresent() && dbConfig.get().getEnabled()) {
            log.debug("使用数据库LLM配置: {}", dbConfig.get().getProvider());
            return dbConfig.get();
        }
        
        // 2. 回退到YAML配置
        log.debug("使用YAML兜底LLM配置: {}", llmProperties.getProvider());
        return buildConfigFromYaml(llmProperties.getProvider());
    }
    
    /**
     * 获取指定提供商的有效配置
     * 优先级：数据库配置 > YAML配置
     */
    public LlmConfig getEffectiveConfig(String provider) {
        // 1. 尝试从数据库获取
        Optional<LlmConfig> dbConfig = llmConfigRepository.findByProvider(provider);
        if (dbConfig.isPresent() && dbConfig.get().getEnabled() 
                && dbConfig.get().getApiKey() != null && !dbConfig.get().getApiKey().isEmpty()) {
            return dbConfig.get();
        }
        
        // 2. 回退到YAML配置
        return buildConfigFromYaml(provider);
    }
    
    /**
     * 从YAML构建配置对象
     */
    private LlmConfig buildConfigFromYaml(String provider) {
        LlmProperties.ProviderConfig yamlConfig = switch (provider.toLowerCase()) {
            case "openai" -> llmProperties.getOpenai();
            case "gemini" -> llmProperties.getGemini();
            default -> llmProperties.getQwen();
        };
        
        return LlmConfig.builder()
                .provider(provider)
                .displayName(getDisplayName(provider))
                .apiKey(yamlConfig.getApiKey())
                .apiUrl(yamlConfig.getApiUrl())
                .modelName(yamlConfig.getModel())
                .enabled(true)
                .isActive(provider.equalsIgnoreCase(llmProperties.getProvider()))
                .temperature(0.8)
                .maxTokens(2000)
                .timeoutSeconds(60)
                .description("YAML配置")
                .build();
    }
    
    private String getDisplayName(String provider) {
        return switch (provider.toLowerCase()) {
            case "openai" -> "OpenAI GPT";
            case "gemini" -> "Google Gemini";
            case "qwen" -> "通义千问";
            default -> provider;
        };
    }
    
    /**
     * 创建LLM配置
     */
    @Transactional
    public LlmConfig createConfig(LlmConfig config) {
        // 检查是否已存在
        if (llmConfigRepository.findByProvider(config.getProvider()).isPresent()) {
            throw new RuntimeException("该提供商配置已存在: " + config.getProvider());
        }
        return llmConfigRepository.save(config);
    }
    
    /**
     * 更新LLM配置
     */
    @Transactional
    public LlmConfig updateConfig(Long id, LlmConfig newConfig) {
        LlmConfig existing = llmConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        
        // 只更新非null字段，避免覆盖原有数据（特别是apiKey）
        if (newConfig.getDisplayName() != null) {
            existing.setDisplayName(newConfig.getDisplayName());
        }
        // apiKey 特殊处理：只有明确提供时才更新（前端只在输入新密钥时发送）
        if (newConfig.getApiKey() != null && !newConfig.getApiKey().isEmpty()) {
            existing.setApiKey(newConfig.getApiKey());
        }
        if (newConfig.getApiUrl() != null) {
            existing.setApiUrl(newConfig.getApiUrl());
        }
        if (newConfig.getModelName() != null) {
            existing.setModelName(newConfig.getModelName());
        }
        if (newConfig.getEnabled() != null) {
            existing.setEnabled(newConfig.getEnabled());
        }
        if (newConfig.getTemperature() != null) {
            existing.setTemperature(newConfig.getTemperature());
        }
        if (newConfig.getMaxTokens() != null) {
            existing.setMaxTokens(newConfig.getMaxTokens());
        }
        if (newConfig.getTimeoutSeconds() != null) {
            existing.setTimeoutSeconds(newConfig.getTimeoutSeconds());
        }
        if (newConfig.getDescription() != null) {
            existing.setDescription(newConfig.getDescription());
        }
        if (newConfig.getSortOrder() != null) {
            existing.setSortOrder(newConfig.getSortOrder());
        }
        
        return llmConfigRepository.save(existing);
    }
    
    /**
     * 设置激活的LLM提供商
     */
    @Transactional
    public LlmConfig setActiveProvider(Long id) {
        // 先将所有配置设为非激活
        List<LlmConfig> allConfigs = llmConfigRepository.findAll();
        for (LlmConfig config : allConfigs) {
            config.setIsActive(false);
        }
        llmConfigRepository.saveAll(allConfigs);
        
        // 设置指定配置为激活
        LlmConfig target = llmConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        target.setIsActive(true);
        return llmConfigRepository.save(target);
    }
    
    /**
     * 设置激活的LLM提供商（按provider名称）
     */
    @Transactional
    public LlmConfig setActiveProvider(String provider) {
        // 先将所有配置设为非激活
        List<LlmConfig> allConfigs = llmConfigRepository.findAll();
        for (LlmConfig config : allConfigs) {
            config.setIsActive(false);
        }
        llmConfigRepository.saveAll(allConfigs);
        
        // 设置指定配置为激活
        LlmConfig target = llmConfigRepository.findByProvider(provider)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + provider));
        target.setIsActive(true);
        return llmConfigRepository.save(target);
    }
    
    /**
     * 删除LLM配置
     */
    @Transactional
    public void deleteConfig(Long id) {
        llmConfigRepository.deleteById(id);
    }
    
    /**
     * 测试LLM连接
     */
    public boolean testConnection(Long id) {
        // 这里可以实现实际的连接测试
        return true;
    }
}
