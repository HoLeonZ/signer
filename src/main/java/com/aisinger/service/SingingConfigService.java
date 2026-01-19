package com.aisinger.service;

import com.aisinger.entity.SingingConfig;
import com.aisinger.repository.SingingConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 演唱配置服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SingingConfigService {
    
    private final SingingConfigRepository singingConfigRepository;
    
    /**
     * 获取所有启用的配置
     */
    public List<SingingConfig> getAllConfigs() {
        return singingConfigRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    /**
     * 获取所有配置（包括禁用的）
     */
    public List<SingingConfig> getAllConfigsIncludeDisabled() {
        return singingConfigRepository.findAll();
    }
    
    /**
     * 根据ID获取配置
     */
    public Optional<SingingConfig> getConfigById(Long id) {
        return singingConfigRepository.findById(id);
    }
    
    /**
     * 根据分类获取配置
     */
    public List<SingingConfig> getConfigsByCategory(String category) {
        return singingConfigRepository.findByCategoryAndEnabledTrueOrderBySortOrderAsc(category);
    }
    
    /**
     * 获取系统预设
     */
    public List<SingingConfig> getPresets() {
        return singingConfigRepository.findByIsPresetTrueAndEnabledTrueOrderBySortOrderAsc();
    }
    
    /**
     * 获取用户自定义配置
     */
    public List<SingingConfig> getCustomConfigs() {
        return singingConfigRepository.findByIsPresetFalseAndEnabledTrueOrderBySortOrderAsc();
    }
    
    /**
     * 获取热门配置
     */
    public List<SingingConfig> getPopularConfigs() {
        return singingConfigRepository.findByEnabledTrueOrderByUseCountDesc();
    }
    
    /**
     * 搜索配置
     */
    public List<SingingConfig> searchConfigs(String keyword) {
        return singingConfigRepository.findByNameContainingAndEnabledTrue(keyword);
    }
    
    /**
     * 创建配置
     */
    @Transactional
    public SingingConfig createConfig(SingingConfig config) {
        if (config.getSortOrder() == null) {
            config.setSortOrder(0);
        }
        if (config.getIsPreset() == null) {
            config.setIsPreset(false);
        }
        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        if (config.getUseCount() == null) {
            config.setUseCount(0);
        }
        return singingConfigRepository.save(config);
    }
    
    /**
     * 更新配置
     */
    @Transactional
    public SingingConfig updateConfig(Long id, SingingConfig newConfig) {
        SingingConfig existing = singingConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        
        // 基本信息
        existing.setName(newConfig.getName());
        existing.setNameEn(newConfig.getNameEn());
        existing.setDescription(newConfig.getDescription());
        existing.setCategory(newConfig.getCategory());
        existing.setUseCase(newConfig.getUseCase());
        
        // 节奏控制
        existing.setDefaultBpm(newConfig.getDefaultBpm());
        existing.setTimeSignature(newConfig.getTimeSignature());
        existing.setSwingFeel(newConfig.getSwingFeel());
        existing.setTimingOffset(newConfig.getTimingOffset());
        existing.setAutoBreath(newConfig.getAutoBreath());
        existing.setBreathStrength(newConfig.getBreathStrength());
        
        // 力度控制
        existing.setBaseVolume(newConfig.getBaseVolume());
        existing.setDynamicsMin(newConfig.getDynamicsMin());
        existing.setDynamicsMax(newConfig.getDynamicsMax());
        existing.setAttackSpeed(newConfig.getAttackSpeed());
        existing.setReleaseSpeed(newConfig.getReleaseSpeed());
        existing.setAutoDynamics(newConfig.getAutoDynamics());
        existing.setAccentStrength(newConfig.getAccentStrength());
        
        // 发音控制
        existing.setArticulationClarity(newConfig.getArticulationClarity());
        existing.setLegatoAmount(newConfig.getLegatoAmount());
        existing.setConsonantStrength(newConfig.getConsonantStrength());
        existing.setVowelLength(newConfig.getVowelLength());
        existing.setEndingStyle(newConfig.getEndingStyle());
        existing.setPronunciationStyle(newConfig.getPronunciationStyle());
        
        // 音高控制
        existing.setPitchShift(newConfig.getPitchShift());
        existing.setPortamentoEnabled(newConfig.getPortamentoEnabled());
        existing.setPortamentoTime(newConfig.getPortamentoTime());
        existing.setPortamentoRange(newConfig.getPortamentoRange());
        existing.setPitchCorrection(newConfig.getPitchCorrection());
        existing.setPitchDrift(newConfig.getPitchDrift());
        
        // 颤音控制
        existing.setVibratoDepth(newConfig.getVibratoDepth());
        existing.setVibratoRate(newConfig.getVibratoRate());
        existing.setVibratoDelay(newConfig.getVibratoDelay());
        existing.setVibratoAttack(newConfig.getVibratoAttack());
        existing.setAutoVibrato(newConfig.getAutoVibrato());
        existing.setAutoVibratoThreshold(newConfig.getAutoVibratoThreshold());
        
        // 音色控制
        existing.setBreathiness(newConfig.getBreathiness());
        existing.setTension(newConfig.getTension());
        existing.setBrightness(newConfig.getBrightness());
        existing.setGenderFactor(newConfig.getGenderFactor());
        existing.setResonanceType(newConfig.getResonanceType());
        existing.setNasality(newConfig.getNasality());
        
        // 效果控制
        existing.setReverbAmount(newConfig.getReverbAmount());
        existing.setReverbType(newConfig.getReverbType());
        existing.setDelayAmount(newConfig.getDelayAmount());
        existing.setHarmonyEnabled(newConfig.getHarmonyEnabled());
        existing.setHarmonyType(newConfig.getHarmonyType());
        existing.setHarmonyVolume(newConfig.getHarmonyVolume());
        existing.setChorusAmount(newConfig.getChorusAmount());
        
        // 状态控制
        existing.setEnabled(newConfig.getEnabled());
        existing.setSortOrder(newConfig.getSortOrder());
        
        return singingConfigRepository.save(existing);
    }
    
    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(Long id) {
        singingConfigRepository.deleteById(id);
    }
    
    /**
     * 增加使用次数
     */
    @Transactional
    public void incrementUseCount(Long id) {
        singingConfigRepository.findById(id).ifPresent(config -> {
            config.setUseCount(config.getUseCount() + 1);
            singingConfigRepository.save(config);
        });
    }
    
    /**
     * 复制配置（基于现有配置创建新配置）
     */
    @Transactional
    public SingingConfig duplicateConfig(Long id, String newName) {
        SingingConfig original = singingConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        
        SingingConfig copy = SingingConfig.builder()
                .name(newName != null ? newName : original.getName() + " (副本)")
                .nameEn(original.getNameEn())
                .description(original.getDescription())
                .category(original.getCategory())
                .useCase(original.getUseCase())
                // 节奏
                .defaultBpm(original.getDefaultBpm())
                .timeSignature(original.getTimeSignature())
                .swingFeel(original.getSwingFeel())
                .timingOffset(original.getTimingOffset())
                .autoBreath(original.getAutoBreath())
                .breathStrength(original.getBreathStrength())
                // 力度
                .baseVolume(original.getBaseVolume())
                .dynamicsMin(original.getDynamicsMin())
                .dynamicsMax(original.getDynamicsMax())
                .attackSpeed(original.getAttackSpeed())
                .releaseSpeed(original.getReleaseSpeed())
                .autoDynamics(original.getAutoDynamics())
                .accentStrength(original.getAccentStrength())
                // 发音
                .articulationClarity(original.getArticulationClarity())
                .legatoAmount(original.getLegatoAmount())
                .consonantStrength(original.getConsonantStrength())
                .vowelLength(original.getVowelLength())
                .endingStyle(original.getEndingStyle())
                .pronunciationStyle(original.getPronunciationStyle())
                // 音高
                .pitchShift(original.getPitchShift())
                .portamentoEnabled(original.getPortamentoEnabled())
                .portamentoTime(original.getPortamentoTime())
                .portamentoRange(original.getPortamentoRange())
                .pitchCorrection(original.getPitchCorrection())
                .pitchDrift(original.getPitchDrift())
                // 颤音
                .vibratoDepth(original.getVibratoDepth())
                .vibratoRate(original.getVibratoRate())
                .vibratoDelay(original.getVibratoDelay())
                .vibratoAttack(original.getVibratoAttack())
                .autoVibrato(original.getAutoVibrato())
                .autoVibratoThreshold(original.getAutoVibratoThreshold())
                // 音色
                .breathiness(original.getBreathiness())
                .tension(original.getTension())
                .brightness(original.getBrightness())
                .genderFactor(original.getGenderFactor())
                .resonanceType(original.getResonanceType())
                .nasality(original.getNasality())
                // 效果
                .reverbAmount(original.getReverbAmount())
                .reverbType(original.getReverbType())
                .delayAmount(original.getDelayAmount())
                .harmonyEnabled(original.getHarmonyEnabled())
                .harmonyType(original.getHarmonyType())
                .harmonyVolume(original.getHarmonyVolume())
                .chorusAmount(original.getChorusAmount())
                // 状态
                .isPreset(false)
                .enabled(true)
                .sortOrder(0)
                .useCount(0)
                .build();
        
        return singingConfigRepository.save(copy);
    }
}
