package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.dto.JamendoSearchRequest;
import com.aisinger.dto.JamendoTrackDTO;
import com.aisinger.service.JamendoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Jamendo音乐库控制器
 */
@RestController
@RequestMapping("/api/jamendo")
@RequiredArgsConstructor
public class JamendoController {
    
    private final JamendoService jamendoService;
    
    /**
     * 搜索歌曲
     */
    @GetMapping("/search")
    public ApiResponse<List<JamendoTrackDTO>> searchTracks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String artistName,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String mood,
            @RequestParam(required = false) String speed,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String vocalInstrumental,
            @RequestParam(required = false) Boolean probackground,
            @RequestParam(defaultValue = "relevance") String orderBy,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        
        JamendoSearchRequest request = JamendoSearchRequest.builder()
                .search(search)
                .artistName(artistName)
                .tags(tags)
                .mood(mood)
                .speed(speed)
                .lang(lang)
                .vocalInstrumental(vocalInstrumental)
                .probackground(probackground)
                .orderBy(orderBy)
                .limit(limit)
                .offset(offset)
                .includeMusicinfo(true)
                .build();
        
        List<JamendoTrackDTO> tracks = jamendoService.searchTracks(request);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 获取热门歌曲
     */
    @GetMapping("/popular")
    public ApiResponse<List<JamendoTrackDTO>> getPopularTracks(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        
        List<JamendoTrackDTO> tracks = jamendoService.getPopularTracks(limit, offset);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 获取最新歌曲
     */
    @GetMapping("/latest")
    public ApiResponse<List<JamendoTrackDTO>> getLatestTracks(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        
        List<JamendoTrackDTO> tracks = jamendoService.getLatestTracks(limit, offset);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 按流派获取歌曲
     */
    @GetMapping("/genre/{genre}")
    public ApiResponse<List<JamendoTrackDTO>> getTracksByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        
        List<JamendoTrackDTO> tracks = jamendoService.getTracksByGenre(genre, limit, offset);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 按情绪获取歌曲
     */
    @GetMapping("/mood/{mood}")
    public ApiResponse<List<JamendoTrackDTO>> getTracksByMood(
            @PathVariable String mood,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        
        List<JamendoTrackDTO> tracks = jamendoService.getTracksByMood(mood, limit, offset);
        return ApiResponse.success(tracks);
    }
    
    /**
     * 获取单首歌曲详情
     */
    @GetMapping("/track/{trackId}")
    public ApiResponse<JamendoTrackDTO> getTrackById(@PathVariable String trackId) {
        JamendoTrackDTO track = jamendoService.getTrackById(trackId);
        if (track != null) {
            return ApiResponse.success(track);
        }
        return ApiResponse.error("歌曲未找到");
    }
    
    /**
     * 获取可用的流派列表
     */
    @GetMapping("/genres")
    public ApiResponse<List<String>> getAvailableGenres() {
        return ApiResponse.success(jamendoService.getAvailableGenres());
    }
    
    /**
     * 获取可用的情绪列表
     */
    @GetMapping("/moods")
    public ApiResponse<List<String>> getAvailableMoods() {
        return ApiResponse.success(jamendoService.getAvailableMoods());
    }
    
    /**
     * 获取服务状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getServiceStatus() {
        return ApiResponse.success(jamendoService.getServiceStatus());
    }
}
