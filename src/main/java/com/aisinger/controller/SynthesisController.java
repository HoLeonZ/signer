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
    
    /**
     * 快速试听预览 - 用于验证配置效果
     * 使用OpenAI TTS进行真实语音合成
     */
    @PostMapping("/preview")
    public ApiResponse<SynthesisResult> previewSynthesis(@RequestBody PreviewRequest request) {
        // 构建合成请求
        SynthesisRequest synthRequest = SynthesisRequest.builder()
                .lyrics(request.getText() != null ? request.getText() : "你好，这是一段测试语音。")
                .vibratoDepth(request.getVibratoDepth() != null ? request.getVibratoDepth() : 50)
                .vibratoRate(request.getVibratoRate() != null ? request.getVibratoRate() : 50)
                .breathiness(request.getBreathiness() != null ? request.getBreathiness() : 30)
                .tension(request.getTension() != null ? request.getTension() : 50)
                .brightness(request.getBrightness() != null ? request.getBrightness() : 50)
                .genderFactor(request.getGenderFactor() != null ? request.getGenderFactor() : 50)
                .emotionIntensity(request.getEmotionIntensity() != null ? request.getEmotionIntensity() : 50)
                .tempoFactor(request.getTempoFactor() != null ? request.getTempoFactor() : 1.0)
                .build();
        
        // 使用OpenAI TTS引擎
        SynthesisResult result = synthesisService.synthesizeWithEngine("openai-tts", synthRequest);
        
        if (result.isSuccess()) {
            return ApiResponse.success("试听生成成功", result);
        } else {
            // 如果OpenAI不可用，返回提示信息
            return ApiResponse.error("试听生成失败: " + result.getErrorMessage() + 
                    "。请在系统设置中配置OpenAI API Key。");
        }
    }
    
    /**
     * 试听预览请求
     */
    @lombok.Data
    public static class PreviewRequest {
        private String text;           // 试听文本
        private Integer vibratoDepth;  // 颤音深度
        private Integer vibratoRate;   // 颤音速率
        private Integer breathiness;   // 气声程度
        private Integer tension;       // 张力
        private Integer brightness;    // 明亮度
        private Integer genderFactor;  // 性别因子
        private Integer emotionIntensity; // 情绪强度
        private Double tempoFactor;    // 节奏因子
        private Long techniqueId;      // 技巧ID
        private Long emotionId;        // 情绪ID
    }
}
