package com.aisinger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Jamendo搜索请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamendoSearchRequest {
    
    /**
     * 搜索关键词
     */
    private String search;
    
    /**
     * 艺术家名称
     */
    private String artistName;
    
    /**
     * 专辑名称
     */
    private String albumName;
    
    /**
     * 标签/流派（逗号分隔）
     */
    private String tags;
    
    /**
     * 情绪标签
     */
    private String mood;
    
    /**
     * 速度: low, medium, high
     */
    private String speed;
    
    /**
     * 乐器类型
     */
    private String instruments;
    
    /**
     * 语言
     */
    private String lang;
    
    /**
     * 人声/纯音乐: vocal, instrumental
     */
    private String vocalInstrumental;
    
    /**
     * 性别: male, female
     */
    private String gender;
    
    /**
     * 声学/电子: acoustic, electric
     */
    private String acousticelectric;
    
    /**
     * 是否只查询可商用
     */
    private Boolean probackground;
    
    /**
     * 排序方式: relevance, popularity_total, popularity_month, popularity_week, releasedate
     */
    @Builder.Default
    private String orderBy = "relevance";
    
    /**
     * 每页数量
     */
    @Builder.Default
    private Integer limit = 20;
    
    /**
     * 偏移量
     */
    @Builder.Default
    private Integer offset = 0;
    
    /**
     * 是否包含音乐信息
     */
    @Builder.Default
    private Boolean includeMusicinfo = true;
    
    /**
     * 是否包含歌词
     */
    @Builder.Default
    private Boolean includeLyrics = false;
}
