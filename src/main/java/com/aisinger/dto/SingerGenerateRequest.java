package com.aisinger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI一键生成歌手请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingerGenerateRequest {
    
    /**
     * 用户输入的描述/需求
     * 例如："创建一个温柔的女声歌手，擅长唱抒情歌曲，声音像邓紫棋"
     * 或："帮我设计一个摇滚风格的男歌手，声音沙哑有力"
     */
    private String prompt;
    
    /**
     * 参考歌手名称（可选）
     * 可以指定一个参考的真实歌手风格
     */
    private String referenceArtist;
    
    /**
     * 目标语言（可选，默认中文）
     */
    private String language;
}
