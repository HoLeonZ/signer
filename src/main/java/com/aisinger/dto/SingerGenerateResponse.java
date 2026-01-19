package com.aisinger.dto;

import com.aisinger.entity.Singer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成歌手响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingerGenerateResponse {
    
    /**
     * 生成的歌手实体
     */
    private Singer singer;
    
    /**
     * LLM生成的创作说明/设计理念
     */
    private String designNotes;
    
    /**
     * 推荐的歌曲风格
     */
    private String recommendedGenres;
    
    /**
     * 推荐的演唱类型
     */
    private String recommendedSongTypes;
}
