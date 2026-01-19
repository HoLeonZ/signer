package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 演唱情绪实体
 * 包含LLM Prompt描述和音频合成参数
 */
@Entity
@Table(name = "emotions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Emotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ==================== 基本信息 ====================
    
    @Column(name = "emotion_id")
    private String emotionId; // 配置ID（对应YAML中的id）
    
    @Column(nullable = false)
    private String name; // 情绪名称
    
    @Column(name = "name_en")
    private String nameEn; // 英文名称
    
    @Column(length = 500)
    private String description; // 情绪描述
    
    private String category; // 分类：积极、消极、中性
    
    // ==================== LLM Prompt配置 ====================
    
    @Column(name = "prompt_description", length = 500)
    private String promptDescription; // LLM Prompt描述
    
    @Column(name = "prompt_keywords")
    private String promptKeywords; // LLM关键词（逗号分隔）
    
    // ==================== 声音合成参数 ====================
    
    @Column(name = "intensity")
    private Integer intensity = 50; // 情绪强度 0-100
    
    @Column(name = "pitch_variance")
    private Double pitchVariance = 1.0; // 音高变化系数
    
    @Column(name = "energy_multiplier")
    private Double energyMultiplier = 1.0; // 能量乘数
    
    @Column(name = "tempo_factor")
    private Double tempoFactor = 1.0; // 节奏系数
    
    @Column(name = "vibrato_depth_modifier")
    private Double vibratoDepthModifier = 1.0; // 颤音深度修改器
    
    @Column(name = "tension_modifier")
    private Double tensionModifier = 1.0; // 张力修改器
    
    // ==================== UI展示 ====================
    
    @Column(name = "color_code")
    private String colorCode; // 情绪颜色标识
    
    @Column(name = "icon_name")
    private String iconName; // 图标/emoji
    
    // ==================== 元数据 ====================
    
    @Column(name = "ai_parameter_config", length = 2000)
    private String aiParameterConfig; // 额外AI参数配置(JSON格式)
    
    private Boolean enabled = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
