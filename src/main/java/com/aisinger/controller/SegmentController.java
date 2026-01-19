package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.dto.SegmentUpdateRequest;
import com.aisinger.entity.MusicSegment;
import com.aisinger.service.MusicSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/segments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SegmentController {
    
    private final MusicSegmentService segmentService;
    
    @GetMapping("/song/{songId}")
    public ApiResponse<List<MusicSegment>> getSegmentsBySong(@PathVariable Long songId) {
        return ApiResponse.success(segmentService.getSegmentsBySong(songId));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<MusicSegment> getSegmentById(@PathVariable Long id) {
        return segmentService.getSegmentById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("片段不存在"));
    }
    
    @PutMapping("/{id}")
    public ApiResponse<MusicSegment> updateSegment(
            @PathVariable Long id, 
            @RequestBody SegmentUpdateRequest request) {
        return ApiResponse.success("片段更新成功", segmentService.updateSegment(id, request));
    }
    
    @PostMapping("/batch-update")
    public ApiResponse<List<MusicSegment>> batchUpdateSegments(
            @RequestParam List<Long> ids,
            @RequestParam(required = false) Long techniqueId,
            @RequestParam(required = false) Long emotionId) {
        return ApiResponse.success("批量更新成功", 
                segmentService.batchUpdateSegments(ids, techniqueId, emotionId));
    }
}
