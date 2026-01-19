package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 音乐片段实体
 * 每首歌曲可以被分割成多个片段，每个片段可以独立配置演唱技巧和情绪
 */
@Entity
@Table(name = "music_segments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicSegment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;
    
    @Column(name = "segment_order")
    private Integer segmentOrder; // 片段顺序
    
    @Column(name = "segment_type")
    private String segmentType; // 片段类型：前奏、主歌、副歌、桥段、尾声等
    
    @Column(name = "start_time")
    private Double startTime; // 开始时间(秒)
    
    @Column(name = "end_time")
    private Double endTime; // 结束时间(秒)
    
    @Column(length = 2000)
    private String lyrics; // 该片段的歌词
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "technique_id")
    private SingingTechnique technique; // 演唱技巧
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emotion_id")
    private Emotion emotion; // 演唱情绪
    
    @Column(name = "volume_level")
    private Integer volumeLevel = 100; // 音量级别 0-100
    
    @Column(name = "pitch_shift")
    private Integer pitchShift = 0; // 音高偏移 -12到+12半音
    
    private String notes; // 备注
}
