package com.aisinger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 声音合成引擎配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "synthesis")
public class SynthesisProperties {
    
    /**
     * 当前启用的引擎: sovits, vits, diffsvc, mock
     */
    private String activeEngine = "mock";
    
    /**
     * So-VITS-SVC配置
     */
    private SovitsConfig sovits = new SovitsConfig();
    
    /**
     * VITS配置
     */
    private VitsConfig vits = new VitsConfig();
    
    /**
     * Diff-SVC配置
     */
    private DiffsvcConfig diffsvc = new DiffsvcConfig();
    
    /**
     * Mock引擎配置（开发测试用）
     */
    private MockConfig mock = new MockConfig();
    
    // ==================== So-VITS-SVC ====================
    
    @Data
    public static class SovitsConfig {
        private boolean enabled = false;
        private String apiUrl = "http://localhost:5000";
        private String modelsPath = "/models/sovits";
        private int sampleRate = 44100;
        private int hopSize = 512;
        private int defaultSpeaker = 0;
    }
    
    // ==================== VITS ====================
    
    @Data
    public static class VitsConfig {
        private boolean enabled = false;
        private String apiUrl = "http://localhost:5001";
        private String modelsPath = "/models/vits";
        private int sampleRate = 22050;
    }
    
    // ==================== Diff-SVC ====================
    
    @Data
    public static class DiffsvcConfig {
        private boolean enabled = false;
        private String apiUrl = "http://localhost:5002";
        private String modelsPath = "/models/diffsvc";
        private int sampleRate = 44100;
        private int diffusionSteps = 100;
    }
    
    // ==================== Mock ====================
    
    @Data
    public static class MockConfig {
        private boolean enabled = true;
        private int delayMs = 1000;
    }
}
