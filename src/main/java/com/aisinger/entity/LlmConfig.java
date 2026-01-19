package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LLM配置实体
 * 支持多个LLM提供商配置，数据库配置优先于YAML配置
 */
@Entity
@Table(name = "llm_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 提供商标识: qwen, openai, gemini
     */
    @Column(nullable = false, unique = true)
    private String provider;
    
    /**
     * 提供商显示名称
     */
    @Column(name = "display_name")
    private String displayName;
    
    /**
     * API密钥
     */
    @Column(name = "api_key", length = 500)
    private String apiKey;
    
    /**
     * API URL
     */
    @Column(name = "api_url", length = 500)
    private String apiUrl;
    
    /**
     * 模型名称
     */
    @Column(name = "model_name")
    private String modelName;
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = false;
    
    /**
     * 是否为当前激活的提供商
     */
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    /**
     * 温度参数 (0.0-2.0)
     */
    @Column(name = "temperature")
    private Double temperature = 0.8;
    
    /**
     * 最大token数
     */
    @Column(name = "max_tokens")
    private Integer maxTokens = 2000;
    
    /**
     * 请求超时(秒)
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 60;
    
    /**
     * 描述/备注
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
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
