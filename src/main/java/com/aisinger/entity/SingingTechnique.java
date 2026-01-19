package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 演唱技巧实体
 * 包含LLM Prompt描述和音频合成参数
 */
@Entity
@Table(name = "singing_techniques")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingingTechnique {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ==================== 基本信息 ====================
    
    @Column(name = "technique_id")
    private String techniqueId; // 配置ID（对应YAML中的id）
    
    @Column(nullable = false)
    private String name; // 技巧名称
    
    @Column(name = "name_en")
    private String nameEn; // 英文名称
    
    @Column(length = 500)
    private String description; // 技巧描述
    
    private String category; // 分类：气息、发声、装饰音、节奏等
    
    @Column(name = "difficulty_level")
    private Integer difficultyLevel; // 难度等级 1-5
    
    // ==================== LLM Prompt配置 ====================
    
    @Column(name = "prompt_description", length = 500)
    private String promptDescription; // LLM Prompt描述
    
    // ==================== 声音合成参数 ====================
    
    @Column(name = "vibrato_depth")
    private Integer vibratoDepth = 50; // 颤音深度 0-100
    
    @Column(name = "vibrato_rate")
    private Integer vibratoRate = 50; // 颤音速率 0-100
    
    @Column(name = "breathiness")
    private Integer breathiness = 30; // 气声程度 0-100
    
    @Column(name = "tension")
    private Integer tension = 50; // 张力 0-100
    
    @Column(name = "brightness")
    private Integer brightness = 50; // 明亮度 0-100
    
    @Column(name = "phonation_type")
    private String phonationType = "normal"; // 发声类型: normal, breathy, falsetto, mixed, growl, crying
    
    @Column(name = "pitch_bend_range")
    private Integer pitchBendRange = 100; // 音高弯曲范围(cents)
    
    // ==================== 元数据 ====================
    
    @Column(name = "sample_audio_url")
    private String sampleAudioUrl; // 示例音频URL
    
    @Column(name = "ai_parameter_config", length = 2000)
    private String aiParameterConfig; // 额外AI参数配置(JSON格式)
    
    private Boolean enabled = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
