package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Jamendo API配置实体
 * 数据库配置优先于YAML配置
 */
@Entity
@Table(name = "jamendo_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamendoConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 配置名称（默认为"default"）
     */
    @Column(nullable = false, unique = true)
    private String name = "default";
    
    /**
     * 是否启用Jamendo
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * Jamendo API Client ID
     */
    @Column(name = "client_id", length = 200)
    private String clientId;
    
    /**
     * Jamendo API基础URL
     */
    @Column(name = "api_url", length = 500)
    private String apiUrl = "https://api.jamendo.com/v3.0";
    
    /**
     * 音频格式: mp31(96kbps), mp32(VBR), ogg, flac
     */
    @Column(name = "audio_format")
    private String audioFormat = "mp32";
    
    /**
     * 每页默认数量
     */
    @Column(name = "default_page_size")
    private Integer defaultPageSize = 20;
    
    /**
     * 搜索结果最大数量
     */
    @Column(name = "max_results")
    private Integer maxResults = 100;
    
    /**
     * 是否只显示可商用歌曲
     */
    @Column(name = "commercial_only")
    private Boolean commercialOnly = false;
    
    /**
     * 请求超时(秒)
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 30;
    
    /**
     * 描述/备注
     */
    @Column(length = 500)
    private String description;
    
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
