package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.dto.LyricsGenerateRequest;
import com.aisinger.dto.LyricsGenerateResponse;
import com.aisinger.dto.SongCreateRequest;
import com.aisinger.entity.Song;
import com.aisinger.service.LlmService;
import com.aisinger.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SongController {
    
    private final SongService songService;
    private final LlmService llmService;
    
    @GetMapping
    public ApiResponse<List<Song>> getAllSongs() {
        return ApiResponse.success(songService.getAllSongs());
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Song> getSongById(@PathVariable Long id) {
        return songService.getSongById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("歌曲不存在"));
    }
    
    @GetMapping("/singer/{singerId}")
    public ApiResponse<List<Song>> getSongsBySinger(@PathVariable Long singerId) {
        return ApiResponse.success(songService.getSongsBySinger(singerId));
    }
    
    @GetMapping("/style/{style}")
    public ApiResponse<List<Song>> getSongsByStyle(@PathVariable String style) {
        return ApiResponse.success(songService.getSongsByStyle(style));
    }
    
    @GetMapping("/search")
    public ApiResponse<List<Song>> searchSongs(@RequestParam String keyword) {
        return ApiResponse.success(songService.searchSongs(keyword));
    }
    
    @PostMapping
    public ApiResponse<Song> createSong(@Valid @RequestBody SongCreateRequest request) {
        return ApiResponse.success("歌曲创建成功", songService.createSong(request));
    }
    
    @PostMapping("/generate-lyrics")
    public ApiResponse<LyricsGenerateResponse> generateLyrics(@RequestBody LyricsGenerateRequest request) {
        return ApiResponse.success("歌词生成成功", llmService.generateLyrics(request));
    }
    
    @PostMapping("/create-from-generated")
    public ApiResponse<Song> createFromGenerated(
            @RequestParam String title,
            @RequestParam String lyrics,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) Integer bpm,
            @RequestParam(required = false) Long singerId) {
        return ApiResponse.success("AI歌曲创建成功", 
                songService.createGeneratedSong(title, lyrics, style, bpm, singerId));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ApiResponse.success("歌曲删除成功", null);
    }
}
