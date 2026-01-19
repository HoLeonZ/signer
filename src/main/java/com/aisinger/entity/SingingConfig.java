package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 歌曲演唱配置实体
 * 包含节奏、力度、发音、音高、效果等完整的演唱控制参数
 */
@Entity
@Table(name = "singing_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingingConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ==================== 基本信息 ====================
    
    /**
     * 配置名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 英文名称
     */
    @Column(name = "name_en")
    private String nameEn;
    
    /**
     * 配置描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 分类：流行、摇滚、抒情、古风、电子等
     */
    private String category;
    
    /**
     * 适用场景描述
     */
    @Column(name = "use_case", length = 500)
    private String useCase;
    
    // ==================== 节奏控制 ====================
    
    /**
     * 默认BPM（每分钟节拍数）
     */
    @Column(name = "default_bpm")
    private Integer defaultBpm = 120;
    
    /**
     * 节拍类型：4/4, 3/4, 6/8等
     */
    @Column(name = "time_signature")
    private String timeSignature = "4/4";
    
    /**
     * 节奏摇摆感 0-100（0=精确，100=自由）
     */
    @Column(name = "swing_feel")
    private Integer swingFeel = 20;
    
    /**
     * 前置偏移（毫秒，负值=提前，正值=延后）
     */
    @Column(name = "timing_offset")
    private Integer timingOffset = 0;
    
    /**
     * 自动呼吸点检测
     */
    @Column(name = "auto_breath")
    private Boolean autoBreath = true;
    
    /**
     * 呼吸点强度 0-100
     */
    @Column(name = "breath_strength")
    private Integer breathStrength = 50;
    
    // ==================== 力度/动态控制 ====================
    
    /**
     * 基础音量 0-100
     */
    @Column(name = "base_volume")
    private Integer baseVolume = 70;
    
    /**
     * 力度变化范围（最小值）0-100
     */
    @Column(name = "dynamics_min")
    private Integer dynamicsMin = 40;
    
    /**
     * 力度变化范围（最大值）0-100
     */
    @Column(name = "dynamics_max")
    private Integer dynamicsMax = 100;
    
    /**
     * 起音速度（Attack）0-100（0=瞬间，100=缓慢）
     */
    @Column(name = "attack_speed")
    private Integer attackSpeed = 30;
    
    /**
     * 释放速度（Release）0-100
     */
    @Column(name = "release_speed")
    private Integer releaseSpeed = 40;
    
    /**
     * 自动力度曲线
     */
    @Column(name = "auto_dynamics")
    private Boolean autoDynamics = true;
    
    /**
     * 重音强度 0-100
     */
    @Column(name = "accent_strength")
    private Integer accentStrength = 60;
    
    // ==================== 发音/咬字控制 ====================
    
    /**
     * 咬字清晰度 0-100
     */
    @Column(name = "articulation_clarity")
    private Integer articulationClarity = 70;
    
    /**
     * 连音程度 0-100（0=断音，100=完全连音）
     */
    @Column(name = "legato_amount")
    private Integer legatoAmount = 60;
    
    /**
     * 辅音强度 0-100
     */
    @Column(name = "consonant_strength")
    private Integer consonantStrength = 50;
    
    /**
     * 元音延长 0-100
     */
    @Column(name = "vowel_length")
    private Integer vowelLength = 50;
    
    /**
     * 尾音处理：自然衰减、干净截止、渐弱
     */
    @Column(name = "ending_style")
    private String endingStyle = "natural";
    
    /**
     * 发音风格：标准、柔和、有力、懒散
     */
    @Column(name = "pronunciation_style")
    private String pronunciationStyle = "standard";
    
    // ==================== 音高控制 ====================
    
    /**
     * 默认音高偏移（半音）
     */
    @Column(name = "pitch_shift")
    private Integer pitchShift = 0;
    
    /**
     * 滑音启用
     */
    @Column(name = "portamento_enabled")
    private Boolean portamentoEnabled = true;
    
    /**
     * 滑音时间（毫秒）
     */
    @Column(name = "portamento_time")
    private Integer portamentoTime = 80;
    
    /**
     * 滑音范围（半音）
     */
    @Column(name = "portamento_range")
    private Integer portamentoRange = 2;
    
    /**
     * 音准修正强度 0-100
     */
    @Column(name = "pitch_correction")
    private Integer pitchCorrection = 50;
    
    /**
     * 自然音高波动 0-100
     */
    @Column(name = "pitch_drift")
    private Integer pitchDrift = 20;
    
    // ==================== 颤音控制 ====================
    
    /**
     * 颤音深度 0-100
     */
    @Column(name = "vibrato_depth")
    private Integer vibratoDepth = 50;
    
    /**
     * 颤音速率 0-100
     */
    @Column(name = "vibrato_rate")
    private Integer vibratoRate = 50;
    
    /**
     * 颤音延迟（毫秒，从音符开始到颤音出现）
     */
    @Column(name = "vibrato_delay")
    private Integer vibratoDelay = 200;
    
    /**
     * 颤音渐入时间（毫秒）
     */
    @Column(name = "vibrato_attack")
    private Integer vibratoAttack = 100;
    
    /**
     * 自动颤音（长音自动添加）
     */
    @Column(name = "auto_vibrato")
    private Boolean autoVibrato = true;
    
    /**
     * 自动颤音阈值（毫秒，超过此长度自动添加）
     */
    @Column(name = "auto_vibrato_threshold")
    private Integer autoVibratoThreshold = 400;
    
    // ==================== 音色控制 ====================
    
    /**
     * 气声程度 0-100
     */
    private Integer breathiness = 30;
    
    /**
     * 声带张力 0-100
     */
    private Integer tension = 50;
    
    /**
     * 明亮度 0-100
     */
    private Integer brightness = 50;
    
    /**
     * 性别因子 0-100（0=最女性化，100=最男性化）
     */
    @Column(name = "gender_factor")
    private Integer genderFactor = 50;
    
    /**
     * 共鸣类型：chest（胸腔）、head（头腔）、mixed（混合）
     */
    @Column(name = "resonance_type")
    private String resonanceType = "mixed";
    
    /**
     * 鼻音程度 0-100
     */
    @Column(name = "nasality")
    private Integer nasality = 30;
    
    // ==================== 效果控制 ====================
    
    /**
     * 混响量 0-100
     */
    @Column(name = "reverb_amount")
    private Integer reverbAmount = 30;
    
    /**
     * 混响类型：room、hall、plate、spring
     */
    @Column(name = "reverb_type")
    private String reverbType = "room";
    
    /**
     * 延迟量 0-100
     */
    @Column(name = "delay_amount")
    private Integer delayAmount = 0;
    
    /**
     * 和声启用
     */
    @Column(name = "harmony_enabled")
    private Boolean harmonyEnabled = false;
    
    /**
     * 和声类型：third（三度）、fifth（五度）、octave（八度）
     */
    @Column(name = "harmony_type")
    private String harmonyType = "third";
    
    /**
     * 和声音量 0-100
     */
    @Column(name = "harmony_volume")
    private Integer harmonyVolume = 50;
    
    /**
     * 合唱效果 0-100
     */
    @Column(name = "chorus_amount")
    private Integer chorusAmount = 0;
    
    // ==================== 状态控制 ====================
    
    /**
     * 是否为系统预设
     */
    @Column(name = "is_preset")
    private Boolean isPreset = false;
    
    /**
     * 是否启用
     */
    private Boolean enabled = true;
    
    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    /**
     * 使用次数（热度）
     */
    @Column(name = "use_count")
    private Integer useCount = 0;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
