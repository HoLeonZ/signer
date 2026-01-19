package com.aisinger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 片段更新请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentUpdateRequest {
    
    private Long techniqueId;
    private Long emotionId;
    private Integer volumeLevel;
    private Integer pitchShift;
    private String notes;
}
