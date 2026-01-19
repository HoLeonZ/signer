package com.aisinger.controller;

import com.aisinger.config.AiSingerProperties;
import com.aisinger.dto.ApiResponse;
import com.aisinger.synthesis.SynthesisService;
import com.aisinger.synthesis.dto.SynthesisRequest;
import com.aisinger.synthesis.dto.SynthesisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 声音合成控制器
 */
@RestController
@RequestMapping("/api/synthesis")
@RequiredArgsConstructor
public class SynthesisController {
    
    private final SynthesisService synthesisService;
    
    /**
     * 执行声音合成
     */
    @PostMapping("/synthesize")
    public ApiResponse<SynthesisResult> synthesize(@RequestBody SynthesisRequest request) {
        SynthesisResult result = synthesisService.synthesize(request);
        if (result.isSuccess()) {
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(result.getErrorMessage());
        }
    }
    
    /**
     * 使用指定引擎合成
     */
    @PostMapping("/synthesize/{engine}")
    public ApiResponse<SynthesisResult> synthesizeWithEngine(
            @PathVariable String engine,
            @RequestBody SynthesisRequest request) {
        SynthesisResult result = synthesisService.synthesizeWithEngine(engine, request);
        if (result.isSuccess()) {
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(result.getErrorMessage());
        }
    }
    
    /**
     * 获取可用的合成引擎
     */
    @GetMapping("/engines")
    public ApiResponse<Map<String, Object>> getAvailableEngines() {
        return ApiResponse.success(synthesisService.getAvailableEngines());
    }
    
    /**
     * 获取配置化的歌手列表
     */
    @GetMapping("/voices")
    public ApiResponse<List<AiSingerProperties.VoiceConfig>> getConfiguredVoices() {
        return ApiResponse.success(synthesisService.getConfiguredVoices());
    }
    
    /**
     * 获取配置化的技巧列表
     */
    @GetMapping("/techniques")
    public ApiResponse<List<AiSingerProperties.TechniqueConfig>> getConfiguredTechniques() {
        return ApiResponse.success(synthesisService.getConfiguredTechniques());
    }
    
    /**
     * 获取配置化的情绪列表
     */
    @GetMapping("/emotions")
    public ApiResponse<List<AiSingerProperties.EmotionConfig>> getConfiguredEmotions() {
        return ApiResponse.success(synthesisService.getConfiguredEmotions());
    }
}
