package com.aisinger.service;

import com.aisinger.entity.Emotion;
import com.aisinger.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionService {
    
    private final EmotionRepository emotionRepository;
    
    public List<Emotion> getAllEmotions() {
        return emotionRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    public List<Emotion> getAllEmotionsIncludeDisabled() {
        return emotionRepository.findAll();
    }
    
    public Optional<Emotion> getEmotionById(Long id) {
        return emotionRepository.findById(id);
    }
    
    public List<Emotion> getEmotionsByCategory(String category) {
        return emotionRepository.findByCategoryAndEnabledTrue(category);
    }
    
    @Transactional
    public Emotion createEmotion(Emotion emotion) {
        return emotionRepository.save(emotion);
    }
    
    @Transactional
    public Emotion updateEmotion(Long id, Emotion details) {
        Emotion emotion = emotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("情绪不存在: " + id));
        
        // 基本信息
        emotion.setEmotionId(details.getEmotionId());
        emotion.setName(details.getName());
        emotion.setNameEn(details.getNameEn());
        emotion.setDescription(details.getDescription());
        emotion.setCategory(details.getCategory());
        
        // LLM Prompt配置
        emotion.setPromptDescription(details.getPromptDescription());
        emotion.setPromptKeywords(details.getPromptKeywords());
        
        // 合成参数
        emotion.setIntensity(details.getIntensity());
        emotion.setPitchVariance(details.getPitchVariance());
        emotion.setEnergyMultiplier(details.getEnergyMultiplier());
        emotion.setTempoFactor(details.getTempoFactor());
        emotion.setVibratoDepthModifier(details.getVibratoDepthModifier());
        emotion.setTensionModifier(details.getTensionModifier());
        
        // UI
        emotion.setColorCode(details.getColorCode());
        emotion.setIconName(details.getIconName());
        
        // 元数据
        emotion.setAiParameterConfig(details.getAiParameterConfig());
        emotion.setEnabled(details.getEnabled());
        emotion.setSortOrder(details.getSortOrder());
        
        return emotionRepository.save(emotion);
    }
    
    @Transactional
    public void deleteEmotion(Long id) {
        emotionRepository.deleteById(id);
    }
}
