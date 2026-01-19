package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 歌曲模板实体 - 提供预设的歌曲风格模板
 */
@Entity
@Table(name = "song_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // 模板名称
    
    @Column(length = 500)
    private String description; // 模板描述
    
    private String category; // 分类：流行、摇滚、民谣、古风等
    
    @Column(name = "icon_emoji")
    private String iconEmoji; // 图标emoji
    
    @Column(name = "suggested_bpm")
    private Integer suggestedBpm; // 建议BPM
    
    @Column(name = "suggested_key")
    private String suggestedKey; // 建议调式
    
    @Column(name = "mood_keywords")
    private String moodKeywords; // 情绪关键词
    
    @Column(name = "style_prompt", length = 1000)
    private String stylePrompt; // 风格提示词
    
    @Column(name = "structure_template")
    private String structureTemplate; // 结构模板：如 verse,chorus,verse,chorus,bridge,chorus
    
    @Column(name = "example_artists")
    private String exampleArtists; // 参考艺人风格
    
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    
    private Boolean enabled = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "use_count")
    private Integer useCount = 0; // 使用次数统计
}
