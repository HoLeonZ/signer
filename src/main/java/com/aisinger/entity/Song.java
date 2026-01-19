package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 歌曲实体
 */
@Entity
@Table(name = "songs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Song {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 5000)
    private String lyrics; // 完整歌词
    
    @Column(name = "music_style")
    private String musicStyle; // 音乐风格：流行、摇滚、民谣等
    
    private Integer bpm; // 节拍速度
    
    @Column(name = "key_signature")
    private String keySignature; // 调式：C大调、A小调等
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds; // 时长(秒)
    
    @Column(name = "is_generated")
    private Boolean isGenerated = false; // 是否由AI生成
    
    // ==================== 外部数据源字段 ====================
    
    @Column(name = "external_source")
    private String externalSource; // 外部来源：jamendo, spotify等
    
    @Column(name = "external_id")
    private String externalId; // 外部系统ID
    
    @Column(name = "external_url")
    private String externalUrl; // 外部链接
    
    @Column(name = "audio_url")
    private String audioUrl; // 音频URL
    
    @Column(name = "cover_url")
    private String coverUrl; // 封面图URL
    
    @Column(name = "artist")
    private String artist; // 艺术家/原唱
    
    @Column(name = "album")
    private String album; // 专辑
    
    @Column(name = "duration")
    private Integer duration; // 时长(秒)，用于外部歌曲
    
    @Column(name = "license")
    private String license; // 版权许可
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "singer_id")
    private Singer singer; // 关联的AI歌手
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "singing_config_id")
    private SingingConfig singingConfig; // 关联的演唱配置
    
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MusicSegment> segments = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
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
