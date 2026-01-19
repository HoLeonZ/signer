package com.aisinger.service;

import com.aisinger.entity.SingingTechnique;
import com.aisinger.repository.SingingTechniqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechniqueService {
    
    private final SingingTechniqueRepository techniqueRepository;
    
    public List<SingingTechnique> getAllTechniques() {
        return techniqueRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    public List<SingingTechnique> getAllTechniquesIncludeDisabled() {
        return techniqueRepository.findAll();
    }
    
    public Optional<SingingTechnique> getTechniqueById(Long id) {
        return techniqueRepository.findById(id);
    }
    
    public List<SingingTechnique> getTechniquesByCategory(String category) {
        return techniqueRepository.findByCategoryAndEnabledTrue(category);
    }
    
    public List<SingingTechnique> getTechniquesByMaxDifficulty(Integer maxLevel) {
        return techniqueRepository.findByDifficultyLevelLessThanEqualAndEnabledTrue(maxLevel);
    }
    
    @Transactional
    public SingingTechnique createTechnique(SingingTechnique technique) {
        return techniqueRepository.save(technique);
    }
    
    @Transactional
    public SingingTechnique updateTechnique(Long id, SingingTechnique details) {
        SingingTechnique technique = techniqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技巧不存在: " + id));
        
        // 基本信息
        technique.setTechniqueId(details.getTechniqueId());
        technique.setName(details.getName());
        technique.setNameEn(details.getNameEn());
        technique.setDescription(details.getDescription());
        technique.setCategory(details.getCategory());
        technique.setDifficultyLevel(details.getDifficultyLevel());
        
        // LLM Prompt配置
        technique.setPromptDescription(details.getPromptDescription());
        
        // 声音合成参数
        technique.setVibratoDepth(details.getVibratoDepth());
        technique.setVibratoRate(details.getVibratoRate());
        technique.setBreathiness(details.getBreathiness());
        technique.setTension(details.getTension());
        technique.setBrightness(details.getBrightness());
        technique.setPhonationType(details.getPhonationType());
        technique.setPitchBendRange(details.getPitchBendRange());
        
        // 元数据
        technique.setSampleAudioUrl(details.getSampleAudioUrl());
        technique.setAiParameterConfig(details.getAiParameterConfig());
        technique.setEnabled(details.getEnabled());
        technique.setSortOrder(details.getSortOrder());
        
        return techniqueRepository.save(technique);
    }
    
    @Transactional
    public void deleteTechnique(Long id) {
        techniqueRepository.deleteById(id);
    }
}
