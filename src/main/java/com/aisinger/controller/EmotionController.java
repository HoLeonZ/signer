package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.entity.Emotion;
import com.aisinger.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emotions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmotionController {
    
    private final EmotionService emotionService;
    
    @GetMapping
    public ApiResponse<List<Emotion>> getAllEmotions() {
        return ApiResponse.success(emotionService.getAllEmotions());
    }
    
    @GetMapping("/all")
    public ApiResponse<List<Emotion>> getAllEmotionsIncludeDisabled() {
        return ApiResponse.success(emotionService.getAllEmotionsIncludeDisabled());
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Emotion> getEmotionById(@PathVariable Long id) {
        return emotionService.getEmotionById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("情绪不存在"));
    }
    
    @GetMapping("/category/{category}")
    public ApiResponse<List<Emotion>> getEmotionsByCategory(@PathVariable String category) {
        return ApiResponse.success(emotionService.getEmotionsByCategory(category));
    }
    
    @PostMapping
    public ApiResponse<Emotion> createEmotion(@RequestBody Emotion emotion) {
        return ApiResponse.success("情绪创建成功", emotionService.createEmotion(emotion));
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Emotion> updateEmotion(@PathVariable Long id, @RequestBody Emotion emotion) {
        return ApiResponse.success("情绪更新成功", emotionService.updateEmotion(id, emotion));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEmotion(@PathVariable Long id) {
        emotionService.deleteEmotion(id);
        return ApiResponse.success("情绪删除成功", null);
    }
}
