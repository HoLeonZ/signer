package com.aisinger.synthesis;

import com.aisinger.synthesis.dto.SynthesisRequest;
import com.aisinger.synthesis.dto.SynthesisResult;

/**
 * 声音合成引擎接口
 * 所有合成引擎（So-VITS-SVC, VITS, Diff-SVC等）都需要实现此接口
 */
public interface SynthesisEngine {
    
    /**
     * 获取引擎名称
     */
    String getEngineName();
    
    /**
     * 检查引擎是否可用
     */
    boolean isAvailable();
    
    /**
     * 执行语音合成
     * @param request 合成请求
     * @return 合成结果
     */
    SynthesisResult synthesize(SynthesisRequest request);
    
    /**
     * 获取引擎支持的功能
     */
    EngineCapabilities getCapabilities();
    
    /**
     * 引擎能力描述
     */
    class EngineCapabilities {
        private boolean supportsEmotionControl;
        private boolean supportsTechniqueControl;
        private boolean supportsRealtimeSynthesis;
        private boolean supportsPitchShift;
        private boolean supportsTempoChange;
        private int maxDurationSeconds;
        private String[] supportedLanguages;
        
        // Getters and setters
        public boolean isSupportsEmotionControl() { return supportsEmotionControl; }
        public void setSupportsEmotionControl(boolean supportsEmotionControl) { this.supportsEmotionControl = supportsEmotionControl; }
        public boolean isSupportsTechniqueControl() { return supportsTechniqueControl; }
        public void setSupportsTechniqueControl(boolean supportsTechniqueControl) { this.supportsTechniqueControl = supportsTechniqueControl; }
        public boolean isSupportsRealtimeSynthesis() { return supportsRealtimeSynthesis; }
        public void setSupportsRealtimeSynthesis(boolean supportsRealtimeSynthesis) { this.supportsRealtimeSynthesis = supportsRealtimeSynthesis; }
        public boolean isSupportsPitchShift() { return supportsPitchShift; }
        public void setSupportsPitchShift(boolean supportsPitchShift) { this.supportsPitchShift = supportsPitchShift; }
        public boolean isSupportsTempoChange() { return supportsTempoChange; }
        public void setSupportsTempoChange(boolean supportsTempoChange) { this.supportsTempoChange = supportsTempoChange; }
        public int getMaxDurationSeconds() { return maxDurationSeconds; }
        public void setMaxDurationSeconds(int maxDurationSeconds) { this.maxDurationSeconds = maxDurationSeconds; }
        public String[] getSupportedLanguages() { return supportedLanguages; }
        public void setSupportedLanguages(String[] supportedLanguages) { this.supportedLanguages = supportedLanguages; }
    }
}
