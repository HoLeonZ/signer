package com.aisinger.synthesis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 声音合成结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SynthesisResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;
    
    /**
     * 音频数据（Base64编码）
     */
    private String audioData;
    
    /**
     * 音频文件路径（如果保存到文件）
     */
    private String audioPath;
    
    /**
     * 音频URL（如果通过URL访问）
     */
    private String audioUrl;
    
    /**
     * 音频格式
     */
    private String format;
    
    /**
     * 时长（秒）
     */
    private Double duration;
    
    /**
     * 采样率
     */
    private Integer sampleRate;
    
    /**
     * 使用的引擎
     */
    private String engine;
    
    /**
     * 处理时间（毫秒）
     */
    private Long processingTimeMs;
    
    /**
     * 消息（成功或提示信息）
     */
    private String message;
    
    /**
     * 额外元数据
     */
    private java.util.Map<String, Object> metadata;
    
    /**
     * 创建成功结果
     */
    public static SynthesisResult success(String audioUrl, String engine, long processingTimeMs) {
        return SynthesisResult.builder()
                .success(true)
                .audioUrl(audioUrl)
                .engine(engine)
                .processingTimeMs(processingTimeMs)
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static SynthesisResult error(String message) {
        return SynthesisResult.builder()
                .success(false)
                .errorMessage(message)
                .build();
    }
}
