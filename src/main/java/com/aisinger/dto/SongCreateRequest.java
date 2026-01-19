package com.aisinger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建歌曲请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongCreateRequest {
    
    @NotBlank(message = "歌曲标题不能为空")
    private String title;
    
    private String lyrics;
    
    private String musicStyle;
    
    private Integer bpm;
    
    private String keySignature;
    
    private Long singerId;
    
    private Long singingConfigId; // 演唱配置ID
    
    private List<SegmentConfig> segments;
    
    // 外部数据源字段
    private Boolean isGenerated;
    private String externalSource; // jamendo, spotify等
    private String externalId;
    private String externalUrl;
    private String audioUrl;
    private String coverUrl;
    private String artist;
    private String album;
    private Integer duration;
    private String license;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentConfig {
        private Integer segmentOrder;
        private String segmentType;
        private Double startTime;
        private Double endTime;
        private String lyrics;
        private Long techniqueId;
        private Long emotionId;
        private Integer volumeLevel;
        private Integer pitchShift;
    }
}
