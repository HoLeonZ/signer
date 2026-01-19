package com.aisinger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Jamendo API配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jamendo")
public class JamendoProperties {
    
    /**
     * 是否启用Jamendo
     */
    private boolean enabled = true;
    
    /**
     * Jamendo API Client ID
     */
    private String clientId;
    
    /**
     * Jamendo API基础URL
     */
    private String apiUrl = "https://api.jamendo.com/v3.0";
    
    /**
     * 音频格式: mp31(96kbps), mp32(VBR), ogg, flac
     */
    private String audioFormat = "mp32";
    
    /**
     * 每页默认数量
     */
    private int defaultPageSize = 20;
    
    /**
     * 搜索结果最大数量
     */
    private int maxResults = 100;
    
    /**
     * 是否只显示可商用歌曲
     */
    private boolean commercialOnly = false;
}
