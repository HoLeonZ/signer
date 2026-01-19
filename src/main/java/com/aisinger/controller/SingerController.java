package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.dto.SingerGenerateRequest;
import com.aisinger.dto.SingerGenerateResponse;
import com.aisinger.entity.Singer;
import com.aisinger.service.LlmService;
import com.aisinger.service.SingerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/singers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SingerController {
    
    private final SingerService singerService;
    private final LlmService llmService;
    
    @GetMapping
    public ApiResponse<List<Singer>> getAllSingers() {
        return ApiResponse.success(singerService.getAllSingers());
    }
    
    @GetMapping("/all")
    public ApiResponse<List<Singer>> getAllSingersIncludeDisabled() {
        return ApiResponse.success(singerService.getAllSingersIncludeDisabled());
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Singer> getSingerById(@PathVariable Long id) {
        return singerService.getSingerById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("歌手不存在"));
    }
    
    @GetMapping("/voice-type/{voiceType}")
    public ApiResponse<List<Singer>> getSingersByVoiceType(@PathVariable String voiceType) {
        return ApiResponse.success(singerService.getSingersByVoiceType(voiceType));
    }
    
    @GetMapping("/voice-style/{voiceStyle}")
    public ApiResponse<List<Singer>> getSingersByVoiceStyle(@PathVariable String voiceStyle) {
        return ApiResponse.success(singerService.getSingersByVoiceStyle(voiceStyle));
    }
    
    @PostMapping
    public ApiResponse<Singer> createSinger(@RequestBody Singer singer) {
        return ApiResponse.success("歌手创建成功", singerService.createSinger(singer));
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Singer> updateSinger(@PathVariable Long id, @RequestBody Singer singer) {
        return ApiResponse.success("歌手更新成功", singerService.updateSinger(id, singer));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSinger(@PathVariable Long id) {
        singerService.deleteSinger(id);
        return ApiResponse.success("歌手删除成功", null);
    }
    
    /**
     * AI一键生成歌手
     * 根据用户的自然语言描述，使用LLM生成完整的歌手配置
     */
    @PostMapping("/generate")
    public ApiResponse<SingerGenerateResponse> generateSinger(@RequestBody SingerGenerateRequest request) {
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ApiResponse.error("请输入歌手描述");
        }
        
        // 调用LLM生成歌手配置
        SingerGenerateResponse response = llmService.generateSinger(request);
        
        // 保存生成的歌手到数据库
        Singer savedSinger = singerService.createSinger(response.getSinger());
        response.setSinger(savedSinger);
        
        return ApiResponse.success("AI歌手创建成功", response);
    }
    
    /**
     * AI生成歌手预览（不保存）
     * 仅生成配置供用户预览和修改
     */
    @PostMapping("/generate/preview")
    public ApiResponse<SingerGenerateResponse> previewGenerateSinger(@RequestBody SingerGenerateRequest request) {
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ApiResponse.error("请输入歌手描述");
        }
        
        // 调用LLM生成歌手配置（不保存）
        SingerGenerateResponse response = llmService.generateSinger(request);
        
        return ApiResponse.success("歌手配置生成成功", response);
    }
}
