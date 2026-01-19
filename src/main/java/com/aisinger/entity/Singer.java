package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI歌手/声音实体
 * 包含声音特征、音域、支持的语言、演唱能力等配置
 */
@Entity
@Table(name = "singers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Singer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ==================== 基本信息 ====================
    
    @Column(nullable = false)
    private String name; // 歌手名称
    
    @Column(name = "name_en")
    private String nameEn; // 英文名称
    
    @Column(length = 500)
    private String description; // 歌手介绍
    
    @Column(name = "avatar_url")
    private String avatarUrl; // 歌手头像URL
    
    @Column(name = "cover_image_url")
    private String coverImageUrl; // 封面大图URL
    
    // ==================== 声音类型与风格 ====================
    
    @Column(name = "voice_type")
    private String voiceType; // 声音类型：男声、女声、中性、童声
    
    @Column(name = "voice_style")
    private String voiceStyle; // 主要风格：流行、古典、摇滚、R&B、民谣、古风、电子等
    
    @Column(name = "voice_character")
    private String voiceCharacter; // 声音特点：温暖、清澈、磁性、高亢、低沉、空灵等
    
    @Column(name = "suitable_genres")
    private String suitableGenres; // 适合的曲风（逗号分隔）：如"流行,抒情,民谣"
    
    // ==================== 音域与音高 ====================
    
    @Column(name = "vocal_range_low")
    private String vocalRangeLow; // 最低音（如：A2）
    
    @Column(name = "vocal_range_high")
    private String vocalRangeHigh; // 最高音（如：C5）
    
    @Column(name = "tessitura_low")
    private String tessituraLow; // 最佳音区下限
    
    @Column(name = "tessitura_high")
    private String tessituraHigh; // 最佳音区上限
    
    @Column(name = "default_pitch_shift")
    private Integer defaultPitchShift = 0; // 默认音高偏移（半音）
    
    // ==================== 语言支持 ====================
    
    @Column(name = "supported_languages")
    private String supportedLanguages; // 支持的语言：中文,英文,日语,韩语等
    
    @Column(name = "primary_language")
    private String primaryLanguage; // 主要语言
    
    @Column(name = "dialect_support")
    private String dialectSupport; // 支持的方言：粤语,闽南语等
    
    // ==================== 演唱能力 ====================
    
    @Column(name = "technique_strength")
    private String techniqueStrength; // 擅长的技巧：颤音,假声,转音,气声等（逗号分隔）
    
    @Column(name = "emotion_strength")
    private String emotionStrength; // 擅长的情感：深情,激昂,温柔,悲伤等（逗号分隔）
    
    @Column(name = "breath_style")
    private String breathStyle; // 气息风格：自然,轻柔,有力
    
    @Column(name = "articulation_style")
    private String articulationStyle; // 咬字风格：清晰,柔和,有力
    
    // ==================== 声音引擎配置 ====================
    
    @Column(name = "voice_model_path")
    private String voiceModelPath; // AI声音模型路径
    
    @Column(name = "voice_model_version")
    private String voiceModelVersion; // 模型版本
    
    @Column(name = "voice_engine")
    private String voiceEngine; // 使用的声音引擎：sovits, vits, diff-svc等
    
    @Column(name = "model_config_json", length = 2000)
    private String modelConfigJson; // 模型配置参数（JSON格式）
    
    // ==================== 默认参数 ====================
    
    @Column(name = "default_vibrato_depth")
    private Integer defaultVibratoDepth = 50; // 默认颤音深度 0-100
    
    @Column(name = "default_vibrato_rate")
    private Integer defaultVibratoRate = 50; // 默认颤音速率 0-100
    
    @Column(name = "default_breathiness")
    private Integer defaultBreathiness = 30; // 默认气声程度 0-100
    
    @Column(name = "default_tension")
    private Integer defaultTension = 50; // 默认张力 0-100
    
    @Column(name = "default_brightness")
    private Integer defaultBrightness = 50; // 默认明亮度 0-100
    
    @Column(name = "default_gender_factor")
    private Integer defaultGenderFactor = 50; // 性别因子 0-100 (0最女性化,100最男性化)
    
    // ==================== 试听与展示 ====================
    
    @Column(name = "sample_audio_url")
    private String sampleAudioUrl; // 示例音频URL
    
    @Column(name = "demo_song_ids")
    private String demoSongIds; // 演示歌曲ID列表
    
    @Column(name = "preview_text")
    private String previewText; // 预览试听的文本/歌词
    
    // ==================== 版权与来源 ====================
    
    @Column(name = "creator")
    private String creator; // 创建者/来源
    
    @Column(name = "license_type")
    private String licenseType; // 授权类型：免费,商用,个人等
    
    @Column(name = "license_info", length = 1000)
    private String licenseInfo; // 详细授权说明
    
    @Column(name = "original_artist")
    private String originalArtist; // 原始艺人（如果是基于真人训练）
    
    // ==================== 标签与分类 ====================
    
    @Column(name = "tags")
    private String tags; // 标签（逗号分隔）
    
    @Column(name = "category")
    private String category; // 分类：原创,翻唱,虚拟歌手等
    
    // ==================== 状态与排序 ====================
    
    private Boolean enabled = true; // 是否启用
    
    @Column(name = "is_premium")
    private Boolean isPremium = false; // 是否为付费/高级
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0; // 排序顺序
    
    @Column(name = "popularity")
    private Integer popularity = 0; // 热度/使用次数
}
