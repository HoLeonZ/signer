package com.aisinger.synthesis.engine;

import com.aisinger.config.SynthesisProperties;
import com.aisinger.synthesis.SynthesisEngine;
import com.aisinger.synthesis.dto.SynthesisRequest;
import com.aisinger.synthesis.dto.SynthesisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock合成引擎 - 用于开发测试
 * 模拟真实引擎的行为，返回占位音频
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MockSynthesisEngine implements SynthesisEngine {
    
    private final SynthesisProperties properties;
    
    @Override
    public String getEngineName() {
        return "mock";
    }
    
    @Override
    public boolean isAvailable() {
        return properties.getMock().isEnabled();
    }
    
    @Override
    public SynthesisResult synthesize(SynthesisRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Mock合成引擎 - 开始合成");
        log.info("  歌手: {}", request.getVoiceId());
        log.info("  技巧: {}", request.getTechniqueId());
        log.info("  情绪: {}", request.getEmotionId());
        log.info("  颤音深度: {}", request.getVibratoDepth());
        log.info("  气声程度: {}", request.getBreathiness());
        log.info("  情绪强度: {}", request.getEmotionIntensity());
        
        // 模拟处理延迟
        try {
            Thread.sleep(properties.getMock().getDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        // 返回模拟结果
        return SynthesisResult.builder()
                .success(true)
                .audioUrl("/mock-audio/sample.mp3")
                .format("mp3")
                .duration(request.getDuration() != null ? request.getDuration() : 30.0)
                .sampleRate(request.getSampleRate())
                .engine(getEngineName())
                .processingTimeMs(processingTime)
                .build();
    }
    
    @Override
    public EngineCapabilities getCapabilities() {
        EngineCapabilities caps = new EngineCapabilities();
        caps.setSupportsEmotionControl(true);
        caps.setSupportsTechniqueControl(true);
        caps.setSupportsRealtimeSynthesis(false);
        caps.setSupportsPitchShift(true);
        caps.setSupportsTempoChange(true);
        caps.setMaxDurationSeconds(300);
        caps.setSupportedLanguages(new String[]{"zh", "en", "ja"});
        return caps;
    }
}
