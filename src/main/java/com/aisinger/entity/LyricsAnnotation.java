package com.aisinger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 歌词标注实体 - 用于歌词的拼音、时间轴、发音标注
 */
@Entity
@Table(name = "lyrics_annotations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LyricsAnnotation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", nullable = false)
    private MusicSegment segment;
    
    @Column(name = "word_index")
    private Integer wordIndex; // 字/词的索引位置
    
    @Column(name = "original_text")
    private String originalText; // 原始文字
    
    private String pinyin; // 拼音标注
    
    private String phoneme; // 音素标注（IPA）
    
    @Column(name = "start_time")
    private Double startTime; // 开始时间（毫秒）
    
    @Column(name = "end_time")
    private Double endTime; // 结束时间（毫秒）
    
    @Column(name = "pitch_value")
    private Integer pitchValue; // 音高值（MIDI音符号）
    
    @Column(name = "velocity")
    private Integer velocity; // 力度 0-127
    
    @Column(name = "vibrato_depth")
    private Integer vibratoDepth; // 颤音深度 0-100
    
    @Column(name = "vibrato_rate")
    private Integer vibratoRate; // 颤音速率
    
    @Column(name = "breath_mark")
    private Boolean breathMark = false; // 是否有换气标记
    
    private String notes; // 备注
}
