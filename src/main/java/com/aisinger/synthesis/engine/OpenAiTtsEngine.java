package com.aisinger.synthesis.engine;

import com.aisinger.entity.LlmConfig;
import com.aisinger.repository.LlmConfigRepository;
import com.aisinger.synthesis.SynthesisEngine;
import com.aisinger.synthesis.dto.SynthesisRequest;
import com.aisinger.synthesis.dto.SynthesisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * OpenAI TTS 语音合成引擎
 * 使用OpenAI的TTS API进行真实语音合成
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiTtsEngine implements SynthesisEngine {
    
    private final LlmConfigRepository llmConfigRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final String TTS_API_URL = "https://api.openai.com/v1/audio/speech";
    
    // OpenAI TTS可用的声音
    private static final List<String> AVAILABLE_VOICES = Arrays.asList(
            "alloy", "echo", "fable", "onyx", "nova", "shimmer"
    );
    
    @Override
    public String getEngineName() {
        return "openai-tts";
    }
    
    @Override
    public boolean isAvailable() {
        Optional<LlmConfig> config = llmConfigRepository.findByProvider("openai");
        return config.isPresent() && 
               config.get().getApiKey() != null && 
               !config.get().getApiKey().isEmpty();
    }
    
    @Override
    public SynthesisResult synthesize(SynthesisRequest request) {
        try {
            // 获取OpenAI配置
            LlmConfig config = llmConfigRepository.findByProvider("openai")
                    .orElseThrow(() -> new RuntimeException("OpenAI配置未找到"));
            
            if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
                return SynthesisResult.error("OpenAI API Key未配置");
            }
            
            // 选择声音（根据配置参数调整）
            String voice = selectVoice(request);
            
            // 选择模型和速度
            String model = "tts-1"; // 或 "tts-1-hd" 高清版
            double speed = calculateSpeed(request);
            
            // 构建请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", request.getLyrics());
            requestBody.put("voice", voice);
            requestBody.put("speed", speed);
            requestBody.put("response_format", "mp3");
            
            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("调用OpenAI TTS: voice={}, speed={}, text_length={}", 
                    voice, speed, request.getLyrics().length());
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    TTS_API_URL,
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 保存音频文件
                String fileName = "tts_" + System.currentTimeMillis() + ".mp3";
                Path outputDir = Paths.get("target/audio");
                Files.createDirectories(outputDir);
                Path outputPath = outputDir.resolve(fileName);
                Files.write(outputPath, response.getBody());
                
                log.info("OpenAI TTS合成成功: {}", outputPath);
                
                return SynthesisResult.builder()
                        .success(true)
                        .audioUrl("/audio/" + fileName)
                        .audioPath(outputPath.toString())
                        .duration(estimateDuration(request.getLyrics(), speed))
                        .format("mp3")
                        .sampleRate(24000)
                        .message("合成成功")
                        .metadata(Map.of(
                                "voice", voice,
                                "speed", speed,
                                "model", model,
                                "engine", "openai-tts"
                        ))
                        .build();
            } else {
                return SynthesisResult.error("OpenAI TTS请求失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("OpenAI TTS合成失败", e);
            return SynthesisResult.error("合成失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置参数选择合适的声音
     */
    private String selectVoice(SynthesisRequest request) {
        // 根据性别因子选择
        int genderFactor = request.getGenderFactor() != null ? request.getGenderFactor() : 50;
        
        // 根据明亮度和气息感选择
        int brightness = request.getBrightness() != null ? request.getBrightness() : 50;
        int breathiness = request.getBreathiness() != null ? request.getBreathiness() : 30;
        
        // 声音特点:
        // alloy - 中性，平衡
        // echo - 男性化，低沉
        // fable - 英式，温暖
        // onyx - 男性，深沉有力
        // nova - 女性，年轻活泼
        // shimmer - 女性，柔和
        
        if (genderFactor < 35) {
            // 偏女性
            if (breathiness > 50) {
                return "shimmer"; // 柔和气息
            } else if (brightness > 60) {
                return "nova"; // 明亮活泼
            } else {
                return "fable"; // 温暖
            }
        } else if (genderFactor > 65) {
            // 偏男性
            if (request.getTension() != null && request.getTension() > 60) {
                return "onyx"; // 有力
            } else {
                return "echo"; // 低沉
            }
        } else {
            // 中性
            return "alloy";
        }
    }
    
    /**
     * 根据情绪和节奏因子计算速度
     */
    private double calculateSpeed(SynthesisRequest request) {
        double baseSpeed = 1.0;
        
        // 节奏因子影响
        if (request.getTempoFactor() != null) {
            baseSpeed *= request.getTempoFactor();
        }
        
        // 情绪强度影响
        if (request.getEmotionIntensity() != null) {
            // 高强度情绪略微加速
            if (request.getEmotionIntensity() > 70) {
                baseSpeed *= 1.05;
            } else if (request.getEmotionIntensity() < 30) {
                baseSpeed *= 0.95;
            }
        }
        
        // 限制在合理范围 0.25-4.0
        return Math.max(0.25, Math.min(4.0, baseSpeed));
    }
    
    /**
     * 估算音频时长（秒）
     */
    private double estimateDuration(String text, double speed) {
        // 大约每个字0.3秒，然后除以速度
        int charCount = text.replaceAll("\\s+", "").length();
        return (charCount * 0.3) / speed;
    }
    
    @Override
    public EngineCapabilities getCapabilities() {
        EngineCapabilities capabilities = new EngineCapabilities();
        capabilities.setSupportsEmotionControl(true);
        capabilities.setSupportsTechniqueControl(false);
        capabilities.setSupportsRealtimeSynthesis(false);
        capabilities.setSupportsPitchShift(false);
        capabilities.setSupportsTempoChange(true);
        capabilities.setMaxDurationSeconds(300);
        capabilities.setSupportedLanguages(new String[]{"zh", "en", "ja", "ko", "es", "fr", "de", "it", "pt", "ru"});
        return capabilities;
    }
}
