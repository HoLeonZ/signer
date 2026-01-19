package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 语音合成服务提供商配置实体
 * 支持多个TTS/SVS服务配置
 */
@Entity
@Table(name = "synthesis_provider_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SynthesisProviderConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 提供商标识: elevenlabs, azure, google, amazon, openai, xunfei, baidu, tencent, so-vits-svc, diff-svc, vits
     */
    @Column(nullable = false, unique = true)
    private String provider;
    
    /**
     * 提供商显示名称
     */
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    /**
     * 提供商类型: cloud（云服务）, local（本地部署）, api（第三方API）
     */
    @Column(name = "provider_type")
    private String providerType;
    
    /**
     * 服务类型: tts（文字转语音）, svs（歌声合成）, vc（声音转换）
     */
    @Column(name = "service_type")
    private String serviceType;
    
    /**
     * API密钥
     */
    @Column(name = "api_key", length = 500)
    private String apiKey;
    
    /**
     * API密钥2（某些服务需要多个密钥，如Azure的region）
     */
    @Column(name = "api_key_secondary", length = 500)
    private String apiKeySecondary;
    
    /**
     * API URL / 服务端点
     */
    @Column(name = "api_url", length = 500)
    private String apiUrl;
    
    /**
     * 区域/Region（云服务需要）
     */
    @Column(name = "region")
    private String region;
    
    /**
     * 默认语音/模型ID
     */
    @Column(name = "default_voice")
    private String defaultVoice;
    
    /**
     * 支持的语音列表（JSON格式）
     */
    @Column(name = "available_voices", length = 2000)
    private String availableVoices;
    
    /**
     * 默认采样率
     */
    @Column(name = "sample_rate")
    private Integer sampleRate;
    
    /**
     * 默认输出格式: mp3, wav, ogg, flac
     */
    @Column(name = "output_format")
    private String outputFormat;
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = false;
    
    /**
     * 是否为当前激活的服务
     */
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    /**
     * 请求超时(秒)
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 120;
    
    /**
     * 最大并发请求数
     */
    @Column(name = "max_concurrent")
    private Integer maxConcurrent = 5;
    
    /**
     * 每分钟请求限制
     */
    @Column(name = "rate_limit")
    private Integer rateLimit = 60;
    
    /**
     * 配额/剩余额度
     */
    @Column(name = "quota")
    private String quota;
    
    /**
     * 价格信息
     */
    @Column(name = "pricing_info", length = 500)
    private String pricingInfo;
    
    /**
     * 官网链接
     */
    @Column(name = "website_url", length = 500)
    private String websiteUrl;
    
    /**
     * 文档链接
     */
    @Column(name = "docs_url", length = 500)
    private String docsUrl;
    
    /**
     * 描述/备注
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * 配置状态: pending（待配置）, configured（已配置）, error（配置错误）, testing（测试中）
     */
    @Column(name = "config_status")
    private String configStatus = "pending";
    
    /**
     * 最后测试结果
     */
    @Column(name = "last_test_result", length = 500)
    private String lastTestResult;
    
    /**
     * 最后测试时间
     */
    @Column(name = "last_test_time")
    private LocalDateTime lastTestTime;
    
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
