package com.aisinger.service;

import com.aisinger.entity.SynthesisProviderConfig;
import com.aisinger.repository.SynthesisProviderConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SynthesisProviderConfigService {
    
    private final SynthesisProviderConfigRepository repository;
    
    public List<SynthesisProviderConfig> getAllConfigs() {
        return repository.findAllByOrderBySortOrderAsc();
    }
    
    public List<SynthesisProviderConfig> getEnabledConfigs() {
        return repository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    public List<SynthesisProviderConfig> getConfigsByServiceType(String serviceType) {
        return repository.findByServiceTypeOrderBySortOrderAsc(serviceType);
    }
    
    public Optional<SynthesisProviderConfig> getConfigByProvider(String provider) {
        return repository.findByProvider(provider);
    }
    
    public Optional<SynthesisProviderConfig> getActiveConfig() {
        return repository.findByIsActiveTrue();
    }
    
    @Transactional
    public SynthesisProviderConfig saveConfig(SynthesisProviderConfig config) {
        // 检查是否存在同名提供商
        Optional<SynthesisProviderConfig> existing = repository.findByProvider(config.getProvider());
        if (existing.isPresent() && !existing.get().getId().equals(config.getId())) {
            throw new RuntimeException("提供商标识已存在: " + config.getProvider());
        }
        
        // 设置配置状态
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            config.setConfigStatus("configured");
        }
        
        return repository.save(config);
    }
    
    @Transactional
    public SynthesisProviderConfig updateConfig(Long id, SynthesisProviderConfig config) {
        SynthesisProviderConfig existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        
        // 只更新非null字段，避免覆盖原有数据
        if (config.getDisplayName() != null) {
            existing.setDisplayName(config.getDisplayName());
        }
        if (config.getProviderType() != null) {
            existing.setProviderType(config.getProviderType());
        }
        if (config.getServiceType() != null) {
            existing.setServiceType(config.getServiceType());
        }
        if (config.getApiKey() != null) {
            existing.setApiKey(config.getApiKey());
        }
        if (config.getApiKeySecondary() != null) {
            existing.setApiKeySecondary(config.getApiKeySecondary());
        }
        if (config.getApiUrl() != null) {
            existing.setApiUrl(config.getApiUrl());
        }
        if (config.getRegion() != null) {
            existing.setRegion(config.getRegion());
        }
        if (config.getDefaultVoice() != null) {
            existing.setDefaultVoice(config.getDefaultVoice());
        }
        if (config.getAvailableVoices() != null) {
            existing.setAvailableVoices(config.getAvailableVoices());
        }
        if (config.getSampleRate() != null) {
            existing.setSampleRate(config.getSampleRate());
        }
        if (config.getOutputFormat() != null) {
            existing.setOutputFormat(config.getOutputFormat());
        }
        if (config.getEnabled() != null) {
            existing.setEnabled(config.getEnabled());
        }
        if (config.getTimeoutSeconds() != null) {
            existing.setTimeoutSeconds(config.getTimeoutSeconds());
        }
        if (config.getMaxConcurrent() != null) {
            existing.setMaxConcurrent(config.getMaxConcurrent());
        }
        if (config.getRateLimit() != null) {
            existing.setRateLimit(config.getRateLimit());
        }
        if (config.getQuota() != null) {
            existing.setQuota(config.getQuota());
        }
        if (config.getPricingInfo() != null) {
            existing.setPricingInfo(config.getPricingInfo());
        }
        if (config.getWebsiteUrl() != null) {
            existing.setWebsiteUrl(config.getWebsiteUrl());
        }
        if (config.getDocsUrl() != null) {
            existing.setDocsUrl(config.getDocsUrl());
        }
        if (config.getDescription() != null) {
            existing.setDescription(config.getDescription());
        }
        if (config.getSortOrder() != null) {
            existing.setSortOrder(config.getSortOrder());
        }
        
        // 更新配置状态
        if (existing.getApiKey() != null && !existing.getApiKey().isEmpty()) {
            existing.setConfigStatus("configured");
        } else {
            existing.setConfigStatus("pending");
        }
        
        return repository.save(existing);
    }
    
    @Transactional
    public void switchActiveProvider(String provider) {
        // 取消所有激活状态
        List<SynthesisProviderConfig> allConfigs = repository.findAll();
        for (SynthesisProviderConfig config : allConfigs) {
            config.setIsActive(false);
        }
        repository.saveAll(allConfigs);
        
        // 激活指定提供商
        SynthesisProviderConfig target = repository.findByProvider(provider)
                .orElseThrow(() -> new RuntimeException("提供商不存在: " + provider));
        target.setIsActive(true);
        target.setEnabled(true);
        repository.save(target);
        
        log.info("语音合成服务已切换到: {}", target.getDisplayName());
    }
    
    @Transactional
    public void deleteConfig(Long id) {
        repository.deleteById(id);
    }
    
    @Transactional
    public SynthesisProviderConfig testConnection(Long id) {
        SynthesisProviderConfig config = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        
        // TODO: 实现实际的连接测试逻辑
        config.setConfigStatus("testing");
        config.setLastTestTime(LocalDateTime.now());
        config.setLastTestResult("测试功能开发中");
        
        return repository.save(config);
    }
}
