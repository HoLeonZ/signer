package com.aisinger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM歌词生成请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LyricsGenerateRequest {
    
    private String theme;        // 主题
    private String mood;         // 情绪基调
    private String style;        // 音乐风格
    private String language;     // 语言：中文、英文、日语等
    private Integer verseCount;  // 段落数量
    private Boolean hasChorus;   // 是否包含副歌
    private String keywords;     // 关键词（用逗号分隔）
    private String additionalPrompt; // 额外提示词
}
