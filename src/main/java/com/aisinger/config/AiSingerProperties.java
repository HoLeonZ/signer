package com.aisinger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI歌手平台完整配置属性
 * 包含歌手、技巧、情绪的配置化定义
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai-singer")
public class AiSingerProperties {
    
    /**
     * 预置歌手声库配置列表
     */
    private List<VoiceConfig> voices = new ArrayList<>();
    
    /**
     * 演唱技巧配置列表
     */
    private List<TechniqueConfig> techniques = new ArrayList<>();
    
    /**
     * 演唱情绪配置列表
     */
    private List<EmotionConfig> emotions = new ArrayList<>();
    
    // ==================== 歌手声库配置 ====================
    
    @Data
    public static class VoiceConfig {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String voiceType;       // 女声、男声、中性
        private String voiceStyle;      // 抒情、摇滚、古风等
        private String voiceCharacter;  // 温暖,柔和,细腻
        private String suitableGenres;  // 适合的曲风
        
        // 音域配置
        private VocalRangeConfig vocalRange;
        
        // 语言支持
        private LanguageConfig languages;
        
        // 声音模型配置
        private ModelConfig model;
        
        // 默认合成参数
        private DefaultParams defaults;
        
        // 元数据
        private String avatarUrl;
        private String tags;
        private String category;
        private String licenseType;
        private boolean enabled = true;
        private int sortOrder = 0;
    }
    
    @Data
    public static class VocalRangeConfig {
        private String low;             // 最低音
        private String high;            // 最高音
        private String tessituraLow;    // 最佳音区下限
        private String tessituraHigh;   // 最佳音区上限
    }
    
    @Data
    public static class LanguageConfig {
        private String primary;         // 主要语言
        private String supported;       // 支持的语言
    }
    
    @Data
    public static class ModelConfig {
        private String engine;          // 引擎类型: sovits, vits, diffsvc
        private String path;            // 模型文件路径
        private String config;          // 配置文件路径
        private int speakerId = 0;      // 说话人ID
    }
    
    @Data
    public static class DefaultParams {
        private int vibratoDepth = 50;      // 颤音深度 0-100
        private int vibratoRate = 50;       // 颤音速率 0-100
        private int breathiness = 30;       // 气声程度 0-100
        private int tension = 50;           // 张力 0-100
        private int brightness = 50;        // 明亮度 0-100
        private int genderFactor = 50;      // 性别因子 0-100
    }
    
    // ==================== 技巧配置 ====================
    
    @Data
    public static class TechniqueConfig {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String category;
        private int difficultyLevel = 1;
        
        // 技巧对应的合成参数
        private TechniqueSynthesisParams synthesisParams;
        
        // LLM Prompt描述
        private String promptDescription;
        
        private boolean enabled = true;
        private int sortOrder = 0;
    }
    
    @Data
    public static class TechniqueSynthesisParams {
        private int vibratoDepth = 50;
        private int vibratoRate = 50;
        private int breathiness = 30;
        private int tension = 50;
        private int brightness = 50;
        private String phonationType = "normal";  // normal, breathy, falsetto, mixed, growl, crying
        private int pitchBendRange = 100;         // 音高弯曲范围(cents)
    }
    
    // ==================== 情绪配置 ====================
    
    @Data
    public static class EmotionConfig {
        private String id;
        private String name;
        private String nameEn;
        private String description;
        private String category;        // 积极、消极、中性
        
        // 情绪对应的合成参数调整
        private EmotionSynthesisParams synthesisParams;
        
        // LLM Prompt配置
        private String promptDescription;
        private String promptKeywords;
        
        // UI展示
        private String colorCode;
        private String icon;
        
        private boolean enabled = true;
        private int sortOrder = 0;
    }
    
    @Data
    public static class EmotionSynthesisParams {
        private int intensity = 50;                     // 情绪强度 0-100
        private double pitchVariance = 1.0;             // 音高变化系数
        private double energyMultiplier = 1.0;          // 能量乘数
        private double tempoFactor = 1.0;               // 节奏系数
        private double vibratoDepthModifier = 1.0;      // 颤音深度修改器
        private double tensionModifier = 1.0;           // 张力修改器
    }
}
