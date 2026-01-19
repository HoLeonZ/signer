package com.aisinger.service;

import com.aisinger.entity.Singer;
import com.aisinger.repository.SingerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SingerService {
    
    private final SingerRepository singerRepository;
    
    public List<Singer> getAllSingers() {
        return singerRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    public List<Singer> getAllSingersIncludeDisabled() {
        return singerRepository.findAll();
    }
    
    public Optional<Singer> getSingerById(Long id) {
        return singerRepository.findById(id);
    }
    
    public List<Singer> getSingersByVoiceType(String voiceType) {
        return singerRepository.findByVoiceTypeAndEnabledTrue(voiceType);
    }
    
    public List<Singer> getSingersByVoiceStyle(String voiceStyle) {
        return singerRepository.findByVoiceStyleAndEnabledTrue(voiceStyle);
    }
    
    @Transactional
    public Singer createSinger(Singer singer) {
        return singerRepository.save(singer);
    }
    
    @Transactional
    public Singer updateSinger(Long id, Singer details) {
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("歌手不存在: " + id));
        
        // 基本信息
        singer.setName(details.getName());
        singer.setNameEn(details.getNameEn());
        singer.setDescription(details.getDescription());
        singer.setAvatarUrl(details.getAvatarUrl());
        singer.setCoverImageUrl(details.getCoverImageUrl());
        
        // 声音类型与风格
        singer.setVoiceType(details.getVoiceType());
        singer.setVoiceStyle(details.getVoiceStyle());
        singer.setVoiceCharacter(details.getVoiceCharacter());
        singer.setSuitableGenres(details.getSuitableGenres());
        
        // 音域
        singer.setVocalRangeLow(details.getVocalRangeLow());
        singer.setVocalRangeHigh(details.getVocalRangeHigh());
        singer.setTessituraLow(details.getTessituraLow());
        singer.setTessituraHigh(details.getTessituraHigh());
        singer.setDefaultPitchShift(details.getDefaultPitchShift());
        
        // 语言支持
        singer.setSupportedLanguages(details.getSupportedLanguages());
        singer.setPrimaryLanguage(details.getPrimaryLanguage());
        singer.setDialectSupport(details.getDialectSupport());
        
        // 演唱能力
        singer.setTechniqueStrength(details.getTechniqueStrength());
        singer.setEmotionStrength(details.getEmotionStrength());
        singer.setBreathStyle(details.getBreathStyle());
        singer.setArticulationStyle(details.getArticulationStyle());
        
        // 声音引擎配置
        singer.setVoiceModelPath(details.getVoiceModelPath());
        singer.setVoiceModelVersion(details.getVoiceModelVersion());
        singer.setVoiceEngine(details.getVoiceEngine());
        singer.setModelConfigJson(details.getModelConfigJson());
        
        // 默认参数
        singer.setDefaultVibratoDepth(details.getDefaultVibratoDepth());
        singer.setDefaultVibratoRate(details.getDefaultVibratoRate());
        singer.setDefaultBreathiness(details.getDefaultBreathiness());
        singer.setDefaultTension(details.getDefaultTension());
        singer.setDefaultBrightness(details.getDefaultBrightness());
        singer.setDefaultGenderFactor(details.getDefaultGenderFactor());
        
        // 试听与展示
        singer.setSampleAudioUrl(details.getSampleAudioUrl());
        singer.setDemoSongIds(details.getDemoSongIds());
        singer.setPreviewText(details.getPreviewText());
        
        // 版权与来源
        singer.setCreator(details.getCreator());
        singer.setLicenseType(details.getLicenseType());
        singer.setLicenseInfo(details.getLicenseInfo());
        singer.setOriginalArtist(details.getOriginalArtist());
        
        // 标签与分类
        singer.setTags(details.getTags());
        singer.setCategory(details.getCategory());
        
        // 状态
        singer.setEnabled(details.getEnabled());
        singer.setIsPremium(details.getIsPremium());
        singer.setSortOrder(details.getSortOrder());
        singer.setPopularity(details.getPopularity());
        
        return singerRepository.save(singer);
    }
    
    @Transactional
    public void deleteSinger(Long id) {
        singerRepository.deleteById(id);
    }
}
