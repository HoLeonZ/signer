package com.aisinger.synthesis;

import com.aisinger.config.AiSingerProperties;
import com.aisinger.config.SynthesisProperties;
import com.aisinger.synthesis.dto.SynthesisRequest;
import com.aisinger.synthesis.dto.SynthesisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 声音合成服务
 * 管理多个合成引擎，根据配置选择合适的引擎进行合成
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SynthesisService {
    
    private final SynthesisProperties synthesisProperties;
    private final AiSingerProperties aiSingerProperties;
    private final List<SynthesisEngine> engines;
    
    /**
     * 使用当前活动引擎进行合成
     */
    public SynthesisResult synthesize(SynthesisRequest request) {
        String activeEngine = synthesisProperties.getActiveEngine();
        return synthesizeWithEngine(activeEngine, request);
    }
    
    /**
     * 使用指定引擎进行合成
     */
    public SynthesisResult synthesizeWithEngine(String engineName, SynthesisRequest request) {
        Optional<SynthesisEngine> engine = engines.stream()
                .filter(e -> e.getEngineName().equals(engineName))
                .findFirst();
        
        if (engine.isEmpty()) {
            return SynthesisResult.error("未找到合成引擎: " + engineName);
        }
        
        if (!engine.get().isAvailable()) {
            return SynthesisResult.error("合成引擎不可用: " + engineName);
        }
        
        // 应用配置中的歌手参数
        applyVoiceParams(request);
        
        // 应用配置中的技巧参数
        applyTechniqueParams(request);
        
        // 应用配置中的情绪参数
        applyEmotionParams(request);
        
        log.info("使用引擎 [{}] 进行合成", engineName);
        return engine.get().synthesize(request);
    }
    
    /**
     * 应用歌手配置参数
     */
    private void applyVoiceParams(SynthesisRequest request) {
        if (request.getVoiceId() == null) return;
        
        aiSingerProperties.getVoices().stream()
                .filter(v -> v.getId().equals(request.getVoiceId()))
                .findFirst()
                .ifPresent(voice -> {
                    // 设置模型路径
                    if (request.getModelPath() == null && voice.getModel() != null) {
                        request.setModelPath(voice.getModel().getPath());
                        request.setSpeakerId(voice.getModel().getSpeakerId());
                    }
                    
                    // 应用默认参数（如果请求中没有指定）
                    if (voice.getDefaults() != null) {
                        var defaults = voice.getDefaults();
                        if (request.getVibratoDepth() == 50) {
                            request.setVibratoDepth(defaults.getVibratoDepth());
                        }
                        if (request.getVibratoRate() == 50) {
                            request.setVibratoRate(defaults.getVibratoRate());
                        }
                        if (request.getBreathiness() == 30) {
                            request.setBreathiness(defaults.getBreathiness());
                        }
                        if (request.getTension() == 50) {
                            request.setTension(defaults.getTension());
                        }
                        if (request.getBrightness() == 50) {
                            request.setBrightness(defaults.getBrightness());
                        }
                    }
                    
                    log.debug("应用歌手配置: {}", voice.getName());
                });
    }
    
    /**
     * 应用技巧配置参数
     */
    private void applyTechniqueParams(SynthesisRequest request) {
        if (request.getTechniqueId() == null) return;
        
        aiSingerProperties.getTechniques().stream()
                .filter(t -> t.getId().equals(request.getTechniqueId()))
                .findFirst()
                .ifPresent(technique -> {
                    var params = technique.getSynthesisParams();
                    if (params != null) {
                        // 技巧参数覆盖默认值
                        request.setVibratoDepth(params.getVibratoDepth());
                        request.setVibratoRate(params.getVibratoRate());
                        request.setBreathiness(params.getBreathiness());
                        request.setTension(params.getTension());
                        request.setBrightness(params.getBrightness());
                        request.setPhonationType(params.getPhonationType());
                    }
                    
                    log.debug("应用技巧配置: {}", technique.getName());
                });
    }
    
    /**
     * 应用情绪配置参数
     */
    private void applyEmotionParams(SynthesisRequest request) {
        if (request.getEmotionId() == null) return;
        
        aiSingerProperties.getEmotions().stream()
                .filter(e -> e.getId().equals(request.getEmotionId()))
                .findFirst()
                .ifPresent(emotion -> {
                    var params = emotion.getSynthesisParams();
                    if (params != null) {
                        request.setEmotionIntensity(params.getIntensity());
                        request.setPitchVariance(params.getPitchVariance());
                        request.setEnergyMultiplier(params.getEnergyMultiplier());
                        request.setTempoFactor(params.getTempoFactor());
                        
                        // 修改器应用到现有参数
                        int adjustedVibrato = (int) (request.getVibratoDepth() * params.getVibratoDepthModifier());
                        request.setVibratoDepth(Math.min(100, Math.max(0, adjustedVibrato)));
                        
                        int adjustedTension = (int) (request.getTension() * params.getTensionModifier());
                        request.setTension(Math.min(100, Math.max(0, adjustedTension)));
                    }
                    
                    log.debug("应用情绪配置: {}", emotion.getName());
                });
    }
    
    /**
     * 获取所有可用引擎
     */
    public Map<String, Object> getAvailableEngines() {
        Map<String, Object> result = new HashMap<>();
        result.put("activeEngine", synthesisProperties.getActiveEngine());
        
        List<Map<String, Object>> engineList = engines.stream()
                .map(e -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", e.getEngineName());
                    info.put("available", e.isAvailable());
                    info.put("capabilities", e.getCapabilities());
                    return info;
                })
                .toList();
        
        result.put("engines", engineList);
        return result;
    }
    
    /**
     * 获取配置化的歌手列表
     */
    public List<AiSingerProperties.VoiceConfig> getConfiguredVoices() {
        return aiSingerProperties.getVoices().stream()
                .filter(AiSingerProperties.VoiceConfig::isEnabled)
                .toList();
    }
    
    /**
     * 获取配置化的技巧列表
     */
    public List<AiSingerProperties.TechniqueConfig> getConfiguredTechniques() {
        return aiSingerProperties.getTechniques().stream()
                .filter(AiSingerProperties.TechniqueConfig::isEnabled)
                .toList();
    }
    
    /**
     * 获取配置化的情绪列表
     */
    public List<AiSingerProperties.EmotionConfig> getConfiguredEmotions() {
        return aiSingerProperties.getEmotions().stream()
                .filter(AiSingerProperties.EmotionConfig::isEnabled)
                .toList();
    }
}
