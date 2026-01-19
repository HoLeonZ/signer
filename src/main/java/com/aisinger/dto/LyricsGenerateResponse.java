package com.aisinger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM歌词生成响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LyricsGenerateResponse {
    
    private String title;           // 生成的歌曲标题
    private String fullLyrics;      // 完整歌词
    private List<LyricsSection> sections; // 分段歌词
    private String suggestedStyle;  // 建议的音乐风格
    private Integer suggestedBpm;   // 建议的BPM
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LyricsSection {
        private String type;        // 段落类型：intro, verse, chorus, bridge, outro
        private String content;     // 段落内容
        private String suggestedEmotion; // 建议的情绪
        private String suggestedTechnique; // 建议的技巧
    }
}
