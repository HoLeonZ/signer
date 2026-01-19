package com.aisinger.service;

import com.aisinger.config.JamendoProperties;
import com.aisinger.entity.JamendoConfig;
import com.aisinger.repository.JamendoConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Jamendo配置服务
 * 数据库配置优先于YAML配置
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JamendoConfigService {
    
    private final JamendoConfigRepository jamendoConfigRepository;
    private final JamendoProperties jamendoProperties; // YAML兜底配置
    
    /**
     * 获取Jamendo配置
     * 优先级：数据库配置 > YAML配置
     */
    public JamendoConfig getConfig() {
        // 1. 尝试从数据库获取默认配置
        Optional<JamendoConfig> dbConfig = jamendoConfigRepository.findDefault();
        if (dbConfig.isPresent()) {
            log.debug("使用数据库Jamendo配置");
            return dbConfig.get();
        }
        
        // 2. 回退到YAML配置
        log.debug("使用YAML兜底Jamendo配置");
        return buildConfigFromYaml();
    }
    
    /**
     * 从YAML构建配置对象
     */
    private JamendoConfig buildConfigFromYaml() {
        return JamendoConfig.builder()
                .name("default")
                .enabled(jamendoProperties.isEnabled())
                .clientId(jamendoProperties.getClientId())
                .apiUrl(jamendoProperties.getApiUrl())
                .audioFormat(jamendoProperties.getAudioFormat())
                .defaultPageSize(jamendoProperties.getDefaultPageSize())
                .maxResults(jamendoProperties.getMaxResults())
                .commercialOnly(jamendoProperties.isCommercialOnly())
                .timeoutSeconds(30)
                .description("YAML配置")
                .build();
    }
    
    /**
     * 获取有效的Client ID
     */
    public String getEffectiveClientId() {
        JamendoConfig config = getConfig();
        if (config.getClientId() != null && !config.getClientId().isEmpty()) {
            return config.getClientId();
        }
        return jamendoProperties.getClientId();
    }
    
    /**
     * 检查Jamendo是否启用
     */
    public boolean isEnabled() {
        JamendoConfig config = getConfig();
        return config.getEnabled() != null && config.getEnabled();
    }
    
    /**
     * 保存或更新Jamendo配置
     */
    @Transactional
    public JamendoConfig saveConfig(JamendoConfig config) {
        // 查找现有配置
        Optional<JamendoConfig> existing = jamendoConfigRepository.findByName(config.getName());
        
        if (existing.isPresent()) {
            JamendoConfig toUpdate = existing.get();
            toUpdate.setEnabled(config.getEnabled());
            toUpdate.setClientId(config.getClientId());
            toUpdate.setApiUrl(config.getApiUrl());
            toUpdate.setAudioFormat(config.getAudioFormat());
            toUpdate.setDefaultPageSize(config.getDefaultPageSize());
            toUpdate.setMaxResults(config.getMaxResults());
            toUpdate.setCommercialOnly(config.getCommercialOnly());
            toUpdate.setTimeoutSeconds(config.getTimeoutSeconds());
            toUpdate.setDescription(config.getDescription());
            return jamendoConfigRepository.save(toUpdate);
        } else {
            return jamendoConfigRepository.save(config);
        }
    }
    
    /**
     * 根据ID获取配置
     */
    public Optional<JamendoConfig> getConfigById(Long id) {
        return jamendoConfigRepository.findById(id);
    }
    
    /**
     * 测试Jamendo连接
     */
    public boolean testConnection() {
        // 这里可以实现实际的连接测试
        String clientId = getEffectiveClientId();
        return clientId != null && !clientId.isEmpty();
    }
}
