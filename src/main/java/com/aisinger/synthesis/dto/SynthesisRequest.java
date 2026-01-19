package com.aisinger.synthesis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 声音合成请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SynthesisRequest {
    
    // ==================== 基本信息 ====================
    
    /**
     * 要合成的歌词/文本
     */
    private String lyrics;
    
    /**
     * 音符序列（MIDI格式或自定义格式）
     */
    private String notes;
    
    /**
     * 音频时长（秒）
     */
    private Double duration;
    
    // ==================== 歌手配置 ====================
    
    /**
     * 歌手/声库ID
     */
    private String voiceId;
    
    /**
     * 声音模型路径
     */
    private String modelPath;
    
    /**
     * 说话人ID（多说话人模型）
     */
    private Integer speakerId;
    
    // ==================== 技巧参数 ====================
    
    /**
     * 技巧ID
     */
    private String techniqueId;
    
    /**
     * 颤音深度 0-100
     */
    @Builder.Default
    private Integer vibratoDepth = 50;
    
    /**
     * 颤音速率 0-100
     */
    @Builder.Default
    private Integer vibratoRate = 50;
    
    /**
     * 气声程度 0-100
     */
    @Builder.Default
    private Integer breathiness = 30;
    
    /**
     * 张力 0-100
     */
    @Builder.Default
    private Integer tension = 50;
    
    /**
     * 明亮度 0-100
     */
    @Builder.Default
    private Integer brightness = 50;
    
    /**
     * 发声类型
     */
    @Builder.Default
    private String phonationType = "normal";
    
    // ==================== 情绪参数 ====================
    
    /**
     * 情绪ID
     */
    private String emotionId;
    
    /**
     * 情绪强度 0-100
     */
    @Builder.Default
    private Integer emotionIntensity = 50;
    
    /**
     * 音高变化系数
     */
    @Builder.Default
    private Double pitchVariance = 1.0;
    
    /**
     * 能量乘数
     */
    @Builder.Default
    private Double energyMultiplier = 1.0;
    
    /**
     * 节奏系数
     */
    @Builder.Default
    private Double tempoFactor = 1.0;
    
    /**
     * 性别因子 0-100 (0=女性化, 100=男性化)
     */
    @Builder.Default
    private Integer genderFactor = 50;
    
    // ==================== 音频参数 ====================
    
    /**
     * 音高偏移（半音）
     */
    @Builder.Default
    private Integer pitchShift = 0;
    
    /**
     * 采样率
     */
    @Builder.Default
    private Integer sampleRate = 44100;
    
    /**
     * 输出格式: wav, mp3, ogg
     */
    @Builder.Default
    private String outputFormat = "wav";
}
