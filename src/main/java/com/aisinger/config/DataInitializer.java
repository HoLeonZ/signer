package com.aisinger.config;

import com.aisinger.entity.*;
import com.aisinger.repository.*;
import com.aisinger.service.SynthesisProviderConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化器 - 从配置文件加载预置数据
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final AiSingerProperties aiSingerProperties;
    private final LlmProperties llmProperties;
    private final JamendoProperties jamendoProperties;
    private final SingerRepository singerRepository;
    private final SingingTechniqueRepository techniqueRepository;
    private final EmotionRepository emotionRepository;
    private final SongRepository songRepository;
    private final MusicSegmentRepository segmentRepository;
    private final SongTemplateRepository templateRepository;
    private final LlmConfigRepository llmConfigRepository;
    private final JamendoConfigRepository jamendoConfigRepository;
    private final SingingConfigRepository singingConfigRepository;
    private final SynthesisProviderConfigRepository synthesisProviderConfigRepository;
    
    @Override
    public void run(String... args) {
        initLlmConfigs();
        initJamendoConfig();
        initSynthesisProviders();
        initTechniquesFromConfig();
        initEmotionsFromConfig();
        initSingersFromConfig();
        initTemplates();
        initSingingConfigs();
        initDemoSong();
        log.info("✅ 数据初始化完成！");
    }
    
    /**
     * 初始化LLM配置
     */
    private void initLlmConfigs() {
        if (llmConfigRepository.count() > 0) {
            log.info("LLM配置已存在，跳过初始化");
            return;
        }
        
        // 通义千问配置
        LlmConfig qwenConfig = LlmConfig.builder()
                .provider("qwen")
                .displayName("通义千问")
                .apiKey(llmProperties.getQwen().getApiKey())
                .apiUrl(llmProperties.getQwen().getApiUrl() != null ? 
                        llmProperties.getQwen().getApiUrl() : 
                        "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                .modelName(llmProperties.getQwen().getModel() != null ? 
                        llmProperties.getQwen().getModel() : 
                        "qwen-turbo")
                .enabled(true)
                .isActive("qwen".equalsIgnoreCase(llmProperties.getProvider()))
                .temperature(0.8)
                .maxTokens(2000)
                .timeoutSeconds(60)
                .description("阿里云通义千问大模型，支持中文优化")
                .sortOrder(1)
                .build();
        llmConfigRepository.save(qwenConfig);
        
        // OpenAI配置
        LlmConfig openaiConfig = LlmConfig.builder()
                .provider("openai")
                .displayName("OpenAI GPT")
                .apiKey(llmProperties.getOpenai().getApiKey())
                .apiUrl(llmProperties.getOpenai().getApiUrl() != null ? 
                        llmProperties.getOpenai().getApiUrl() : 
                        "https://api.openai.com/v1/chat/completions")
                .modelName(llmProperties.getOpenai().getModel() != null ? 
                        llmProperties.getOpenai().getModel() : 
                        "gpt-3.5-turbo")
                .enabled(true)
                .isActive("openai".equalsIgnoreCase(llmProperties.getProvider()))
                .temperature(0.8)
                .maxTokens(2000)
                .timeoutSeconds(60)
                .description("OpenAI GPT系列模型，全球领先的AI模型")
                .sortOrder(2)
                .build();
        llmConfigRepository.save(openaiConfig);
        
        // Gemini配置
        LlmConfig geminiConfig = LlmConfig.builder()
                .provider("gemini")
                .displayName("Google Gemini")
                .apiKey(llmProperties.getGemini().getApiKey())
                .apiUrl(llmProperties.getGemini().getApiUrl() != null ? 
                        llmProperties.getGemini().getApiUrl() : 
                        "https://generativelanguage.googleapis.com/v1beta/models")
                .modelName(llmProperties.getGemini().getModel() != null ? 
                        llmProperties.getGemini().getModel() : 
                        "gemini-pro")
                .enabled(true)
                .isActive("gemini".equalsIgnoreCase(llmProperties.getProvider()))
                .temperature(0.8)
                .maxTokens(2000)
                .timeoutSeconds(60)
                .description("Google Gemini大模型，多模态能力强")
                .sortOrder(3)
                .build();
        llmConfigRepository.save(geminiConfig);
        
        log.info("初始化了 3 个LLM配置");
    }
    
    /**
     * 初始化Jamendo配置
     */
    private void initJamendoConfig() {
        if (jamendoConfigRepository.count() > 0) {
            log.info("Jamendo配置已存在，跳过初始化");
            return;
        }
        
        JamendoConfig config = JamendoConfig.builder()
                .name("default")
                .enabled(jamendoProperties.isEnabled())
                .clientId(jamendoProperties.getClientId())
                .apiUrl(jamendoProperties.getApiUrl() != null ? 
                        jamendoProperties.getApiUrl() : 
                        "https://api.jamendo.com/v3.0")
                .audioFormat(jamendoProperties.getAudioFormat() != null ? 
                        jamendoProperties.getAudioFormat() : 
                        "mp32")
                .defaultPageSize(jamendoProperties.getDefaultPageSize() > 0 ? 
                        jamendoProperties.getDefaultPageSize() : 
                        20)
                .maxResults(jamendoProperties.getMaxResults() > 0 ? 
                        jamendoProperties.getMaxResults() : 
                        100)
                .commercialOnly(jamendoProperties.isCommercialOnly())
                .timeoutSeconds(30)
                .description("Jamendo免费音乐库API")
                .build();
        
        jamendoConfigRepository.save(config);
        log.info("初始化了 Jamendo 配置");
    }
    
    /**
     * 初始化语音合成服务提供商配置
     */
    private void initSynthesisProviders() {
        if (synthesisProviderConfigRepository.count() > 0) {
            log.info("语音合成服务配置已存在，跳过初始化");
            return;
        }
        
        // 1. ElevenLabs - 顶级AI语音合成
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("elevenlabs")
                .displayName("ElevenLabs")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://api.elevenlabs.io/v1")
                .sampleRate(44100)
                .outputFormat("mp3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(120)
                .maxConcurrent(5)
                .rateLimit(100)
                .pricingInfo("免费版每月10000字符，付费版$5起")
                .websiteUrl("https://elevenlabs.io")
                .docsUrl("https://docs.elevenlabs.io/api-reference")
                .description("业界领先的AI语音合成服务，支持多语言、情感、声音克隆")
                .configStatus("pending")
                .sortOrder(1)
                .build());
        
        // 2. OpenAI TTS - GPT生态
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("openai-tts")
                .displayName("OpenAI TTS")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://api.openai.com/v1/audio/speech")
                .defaultVoice("alloy")
                .availableVoices("[\"alloy\",\"echo\",\"fable\",\"onyx\",\"nova\",\"shimmer\"]")
                .sampleRate(24000)
                .outputFormat("mp3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(60)
                .maxConcurrent(10)
                .rateLimit(500)
                .pricingInfo("$15/100万字符(TTS-1)，$30/100万字符(TTS-1-HD)")
                .websiteUrl("https://openai.com")
                .docsUrl("https://platform.openai.com/docs/guides/text-to-speech")
                .description("OpenAI官方TTS服务，6种预置声音，支持HD高清模式")
                .configStatus("pending")
                .sortOrder(2)
                .build());
        
        // 3. Azure Speech Services - 微软云
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("azure-speech")
                .displayName("Azure Speech Services")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://{region}.tts.speech.microsoft.com/cognitiveservices/v1")
                .region("eastasia")
                .sampleRate(24000)
                .outputFormat("audio-24khz-48kbitrate-mono-mp3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(60)
                .maxConcurrent(20)
                .rateLimit(200)
                .pricingInfo("免费版每月50万字符，付费版$4/100万字符起")
                .websiteUrl("https://azure.microsoft.com/services/cognitive-services/speech-services/")
                .docsUrl("https://learn.microsoft.com/azure/cognitive-services/speech-service/")
                .description("微软Azure语音服务，400+声音，支持SSML、神经网络语音")
                .configStatus("pending")
                .sortOrder(3)
                .build());
        
        // 4. Google Cloud TTS - 谷歌云
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("google-tts")
                .displayName("Google Cloud TTS")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://texttospeech.googleapis.com/v1")
                .sampleRate(24000)
                .outputFormat("MP3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(60)
                .maxConcurrent(20)
                .rateLimit(1000)
                .pricingInfo("免费版每月100万字符，付费版$4/100万字符起")
                .websiteUrl("https://cloud.google.com/text-to-speech")
                .docsUrl("https://cloud.google.com/text-to-speech/docs")
                .description("Google云端语音合成，支持WaveNet和Neural2高品质声音")
                .configStatus("pending")
                .sortOrder(4)
                .build());
        
        // 5. Amazon Polly - AWS
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("amazon-polly")
                .displayName("Amazon Polly")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://polly.{region}.amazonaws.com")
                .region("ap-northeast-1")
                .sampleRate(22050)
                .outputFormat("mp3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(60)
                .maxConcurrent(20)
                .rateLimit(100)
                .pricingInfo("免费版每月500万字符（首年），付费版$4/100万字符")
                .websiteUrl("https://aws.amazon.com/polly/")
                .docsUrl("https://docs.aws.amazon.com/polly/")
                .description("AWS语音合成服务，支持SSML、Newscaster和Neural声音")
                .configStatus("pending")
                .sortOrder(5)
                .build());
        
        // 6. 科大讯飞 - 国内领先
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("xunfei")
                .displayName("科大讯飞语音合成")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://tts-api.xfyun.cn/v2/tts")
                .sampleRate(16000)
                .outputFormat("audio/mpeg")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(60)
                .maxConcurrent(10)
                .rateLimit(100)
                .pricingInfo("免费版每日500次，付费版低至0.002元/次")
                .websiteUrl("https://www.xfyun.cn/services/online_tts")
                .docsUrl("https://www.xfyun.cn/doc/tts/online_tts/API.html")
                .description("国内领先的语音技术服务商，中文语音合成效果出色")
                .configStatus("pending")
                .sortOrder(6)
                .build());
        
        // 7. 百度语音 - 国内
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("baidu-tts")
                .displayName("百度语音合成")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://tsn.baidu.com/text2audio")
                .sampleRate(16000)
                .outputFormat("mp3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(60)
                .maxConcurrent(10)
                .rateLimit(100)
                .pricingInfo("免费版每日5万次，付费版阶梯计费")
                .websiteUrl("https://ai.baidu.com/tech/speech/tts")
                .docsUrl("https://ai.baidu.com/ai-doc/SPEECH/Qk38y8lrl")
                .description("百度AI开放平台语音合成，支持多种音色和情感")
                .configStatus("pending")
                .sortOrder(7)
                .build());
        
        // 8. 腾讯云语音 - 国内
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("tencent-tts")
                .displayName("腾讯云语音合成")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://tts.tencentcloudapi.com")
                .region("ap-guangzhou")
                .sampleRate(16000)
                .outputFormat("mp3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(60)
                .maxConcurrent(10)
                .rateLimit(100)
                .pricingInfo("免费版每月80万字符，付费版低至0.0006元/字符")
                .websiteUrl("https://cloud.tencent.com/product/tts")
                .docsUrl("https://cloud.tencent.com/document/product/1073")
                .description("腾讯云语音合成服务，支持多种音色、情感和场景")
                .configStatus("pending")
                .sortOrder(8)
                .build());
        
        // 9. So-VITS-SVC - 本地部署歌声合成
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("so-vits-svc")
                .displayName("So-VITS-SVC")
                .providerType("local")
                .serviceType("svs")
                .apiUrl("http://localhost:7860")
                .sampleRate(44100)
                .outputFormat("wav")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(300)
                .maxConcurrent(1)
                .pricingInfo("开源免费，需要GPU本地部署")
                .websiteUrl("https://github.com/svc-develop-team/so-vits-svc")
                .docsUrl("https://github.com/svc-develop-team/so-vits-svc/wiki")
                .description("开源歌声转换模型，可训练自定义声音，适合高品质AI翻唱")
                .configStatus("pending")
                .sortOrder(9)
                .build());
        
        // 10. VITS - 本地部署
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("vits")
                .displayName("VITS")
                .providerType("local")
                .serviceType("tts")
                .apiUrl("http://localhost:5000")
                .sampleRate(22050)
                .outputFormat("wav")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(120)
                .maxConcurrent(2)
                .pricingInfo("开源免费，需要GPU本地部署")
                .websiteUrl("https://github.com/jaywalnut310/vits")
                .docsUrl("https://github.com/jaywalnut310/vits")
                .description("端到端语音合成模型，支持多语言、多说话人")
                .configStatus("pending")
                .sortOrder(10)
                .build());
        
        // 11. Diff-SVC - 本地部署歌声合成
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("diff-svc")
                .displayName("Diff-SVC")
                .providerType("local")
                .serviceType("svs")
                .apiUrl("http://localhost:7861")
                .sampleRate(44100)
                .outputFormat("wav")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(300)
                .maxConcurrent(1)
                .pricingInfo("开源免费，需要GPU本地部署")
                .websiteUrl("https://github.com/prophesier/diff-svc")
                .docsUrl("https://github.com/prophesier/diff-svc")
                .description("基于扩散模型的歌声转换，音质更高更自然")
                .configStatus("pending")
                .sortOrder(11)
                .build());
        
        // 12. Fish Audio - 新兴AI语音
        synthesisProviderConfigRepository.save(SynthesisProviderConfig.builder()
                .provider("fish-audio")
                .displayName("Fish Audio")
                .providerType("cloud")
                .serviceType("tts")
                .apiUrl("https://api.fish.audio/v1")
                .sampleRate(44100)
                .outputFormat("mp3")
                .enabled(false)
                .isActive(false)
                .timeoutSeconds(120)
                .maxConcurrent(5)
                .rateLimit(50)
                .pricingInfo("免费版每月1000次，付费版$10起")
                .websiteUrl("https://fish.audio")
                .docsUrl("https://docs.fish.audio")
                .description("新兴AI语音合成平台，支持声音克隆和多语言")
                .configStatus("pending")
                .sortOrder(12)
                .build());
        
        log.info("初始化了 12 个语音合成服务配置");
    }
    
    /**
     * 从配置文件初始化技巧
     */
    private void initTechniquesFromConfig() {
        List<AiSingerProperties.TechniqueConfig> configs = aiSingerProperties.getTechniques();
        
        if (configs == null || configs.isEmpty()) {
            log.warn("配置文件中未找到技巧配置，使用默认数据");
            initDefaultTechniques();
            return;
        }
        
        for (AiSingerProperties.TechniqueConfig config : configs) {
            var params = config.getSynthesisParams();
            SingingTechnique technique = SingingTechnique.builder()
                    .techniqueId(config.getId())
                    .name(config.getName())
                    .nameEn(config.getNameEn())
                    .description(config.getDescription())
                    .category(config.getCategory())
                    .difficultyLevel(config.getDifficultyLevel())
                    .promptDescription(config.getPromptDescription())
                    // 合成参数
                    .vibratoDepth(params != null ? params.getVibratoDepth() : 50)
                    .vibratoRate(params != null ? params.getVibratoRate() : 50)
                    .breathiness(params != null ? params.getBreathiness() : 30)
                    .tension(params != null ? params.getTension() : 50)
                    .brightness(params != null ? params.getBrightness() : 50)
                    .phonationType(params != null ? params.getPhonationType() : "normal")
                    .pitchBendRange(params != null ? params.getPitchBendRange() : 100)
                    .enabled(config.isEnabled())
                    .sortOrder(config.getSortOrder())
                    .build();
            
            techniqueRepository.save(technique);
        }
        
        log.info("从配置文件初始化了 {} 个演唱技巧", configs.size());
    }
    
    /**
     * 从配置文件初始化情绪
     */
    private void initEmotionsFromConfig() {
        List<AiSingerProperties.EmotionConfig> configs = aiSingerProperties.getEmotions();
        
        if (configs == null || configs.isEmpty()) {
            log.warn("配置文件中未找到情绪配置，使用默认数据");
            initDefaultEmotions();
            return;
        }
        
        for (AiSingerProperties.EmotionConfig config : configs) {
            var params = config.getSynthesisParams();
            Emotion emotion = Emotion.builder()
                    .emotionId(config.getId())
                    .name(config.getName())
                    .nameEn(config.getNameEn())
                    .description(config.getDescription())
                    .category(config.getCategory())
                    .promptDescription(config.getPromptDescription())
                    .promptKeywords(config.getPromptKeywords())
                    // 合成参数
                    .intensity(params != null ? params.getIntensity() : 50)
                    .pitchVariance(params != null ? params.getPitchVariance() : 1.0)
                    .energyMultiplier(params != null ? params.getEnergyMultiplier() : 1.0)
                    .tempoFactor(params != null ? params.getTempoFactor() : 1.0)
                    .vibratoDepthModifier(params != null ? params.getVibratoDepthModifier() : 1.0)
                    .tensionModifier(params != null ? params.getTensionModifier() : 1.0)
                    // UI
                    .colorCode(config.getColorCode())
                    .iconName(config.getIcon())
                    .enabled(config.isEnabled())
                    .sortOrder(config.getSortOrder())
                    .build();
            
            emotionRepository.save(emotion);
        }
        
        log.info("从配置文件初始化了 {} 个演唱情绪", configs.size());
    }
    
    /**
     * 从配置文件初始化歌手
     */
    private void initSingersFromConfig() {
        List<AiSingerProperties.VoiceConfig> configs = aiSingerProperties.getVoices();
        
        if (configs == null || configs.isEmpty()) {
            log.warn("配置文件中未找到歌手配置，使用默认数据");
            initDefaultSingers();
            return;
        }
        
        for (AiSingerProperties.VoiceConfig config : configs) {
            var model = config.getModel();
            var defaults = config.getDefaults();
            var range = config.getVocalRange();
            var langs = config.getLanguages();
            
            Singer singer = Singer.builder()
                    .name(config.getName())
                    .nameEn(config.getNameEn())
                    .description(config.getDescription())
                    .voiceType(config.getVoiceType())
                    .voiceStyle(config.getVoiceStyle())
                    .voiceCharacter(config.getVoiceCharacter())
                    .suitableGenres(config.getSuitableGenres())
                    // 音域
                    .vocalRangeLow(range != null ? range.getLow() : null)
                    .vocalRangeHigh(range != null ? range.getHigh() : null)
                    .tessituraLow(range != null ? range.getTessituraLow() : null)
                    .tessituraHigh(range != null ? range.getTessituraHigh() : null)
                    // 语言
                    .primaryLanguage(langs != null ? langs.getPrimary() : "中文")
                    .supportedLanguages(langs != null ? langs.getSupported() : "中文")
                    // 模型配置
                    .voiceEngine(model != null ? model.getEngine() : null)
                    .voiceModelPath(model != null ? model.getPath() : null)
                    .modelConfigJson(model != null ? model.getConfig() : null)
                    // 默认参数
                    .defaultVibratoDepth(defaults != null ? defaults.getVibratoDepth() : 50)
                    .defaultVibratoRate(defaults != null ? defaults.getVibratoRate() : 50)
                    .defaultBreathiness(defaults != null ? defaults.getBreathiness() : 30)
                    .defaultTension(defaults != null ? defaults.getTension() : 50)
                    .defaultBrightness(defaults != null ? defaults.getBrightness() : 50)
                    .defaultGenderFactor(defaults != null ? defaults.getGenderFactor() : 50)
                    // 元数据
                    .avatarUrl(config.getAvatarUrl())
                    .tags(config.getTags())
                    .category(config.getCategory())
                    .licenseType(config.getLicenseType())
                    .enabled(config.isEnabled())
                    .sortOrder(config.getSortOrder())
                    .build();
            
            singerRepository.save(singer);
        }
        
        log.info("从配置文件初始化了 {} 个AI歌手", configs.size());
    }
    
    // ==================== 默认数据（配置为空时使用）====================
    
    private void initDefaultTechniques() {
        List<SingingTechnique> techniques = Arrays.asList(
            SingingTechnique.builder()
                .techniqueId("natural")
                .name("自然音")
                .nameEn("Natural")
                .description("最基础的演唱方式，声音自然流畅")
                .category("基础")
                .difficultyLevel(1)
                .promptDescription("自然流畅的演唱，不加任何特殊技巧")
                .vibratoDepth(30).vibratoRate(50).breathiness(20)
                .tension(50).brightness(50).phonationType("normal")
                .sortOrder(1).enabled(true).build(),
            SingingTechnique.builder()
                .techniqueId("breathy")
                .name("气声")
                .nameEn("Breathy")
                .description("带有气息感的演唱方式，营造亲密感")
                .category("气息")
                .difficultyLevel(2)
                .promptDescription("带有轻柔气息的演唱，声音柔软亲密")
                .vibratoDepth(20).vibratoRate(40).breathiness(70)
                .tension(30).brightness(40).phonationType("breathy")
                .sortOrder(2).enabled(true).build(),
            SingingTechnique.builder()
                .techniqueId("vibrato")
                .name("颤音")
                .nameEn("Vibrato")
                .description("音高快速轻微波动，增加情感表达")
                .category("装饰音")
                .difficultyLevel(3)
                .promptDescription("使用明显的颤音技巧，增加声音的情感波动")
                .vibratoDepth(80).vibratoRate(70).breathiness(25)
                .tension(55).brightness(55).phonationType("normal")
                .sortOrder(3).enabled(true).build()
        );
        
        techniqueRepository.saveAll(techniques);
        log.info("初始化了 {} 个默认演唱技巧", techniques.size());
    }
    
    private void initDefaultEmotions() {
        List<Emotion> emotions = Arrays.asList(
            Emotion.builder()
                .emotionId("calm")
                .name("平静")
                .nameEn("Calm")
                .description("平和、安宁的情绪状态")
                .category("中性")
                .promptDescription("演唱时保持平静、安宁的情绪，声音舒缓温和")
                .promptKeywords("平静,安宁,舒缓,温和")
                .intensity(30).pitchVariance(0.8).energyMultiplier(0.9)
                .tempoFactor(0.95).vibratoDepthModifier(0.7).tensionModifier(0.8)
                .colorCode("#87CEEB").iconName("☮️")
                .sortOrder(1).enabled(true).build(),
            Emotion.builder()
                .emotionId("happy")
                .name("快乐")
                .nameEn("Happy")
                .description("欢快、愉悦的情绪")
                .category("积极")
                .promptDescription("演唱时表达快乐、欢快的情绪，声音明亮活泼")
                .promptKeywords("快乐,欢快,愉悦,明亮")
                .intensity(70).pitchVariance(1.2).energyMultiplier(1.2)
                .tempoFactor(1.05).vibratoDepthModifier(1.0).tensionModifier(0.9)
                .colorCode("#FFD700").iconName("😊")
                .sortOrder(2).enabled(true).build(),
            Emotion.builder()
                .emotionId("sad")
                .name("忧伤")
                .nameEn("Sad")
                .description("悲伤、忧郁的情绪")
                .category("消极")
                .promptDescription("演唱时表达忧伤、悲伤的情绪，声音低沉忧郁")
                .promptKeywords("忧伤,悲伤,忧郁,哀愁")
                .intensity(50).pitchVariance(0.9).energyMultiplier(0.8)
                .tempoFactor(0.88).vibratoDepthModifier(1.3).tensionModifier(0.75)
                .colorCode("#4169E1").iconName("😢")
                .sortOrder(5).enabled(true).build()
        );
        
        emotionRepository.saveAll(emotions);
        log.info("初始化了 {} 个默认演唱情绪", emotions.size());
    }
    
    private void initDefaultSingers() {
        List<Singer> singers = Arrays.asList(
            Singer.builder()
                .name("晨曦")
                .nameEn("Chenxi")
                .description("温暖柔和的女声，擅长抒情慢歌")
                .voiceType("女声")
                .voiceStyle("抒情")
                .voiceCharacter("温暖,柔和,细腻")
                .suitableGenres("流行,抒情,民谣")
                .vocalRangeLow("A3").vocalRangeHigh("E5")
                .primaryLanguage("中文").supportedLanguages("中文,英文")
                .defaultVibratoDepth(40).defaultBreathiness(45).defaultBrightness(55)
                .avatarUrl("/avatars/chenxi.png")
                .tags("甜美,治愈,抒情")
                .category("虚拟歌手").licenseType("免费")
                .sortOrder(1).enabled(true).build(),
            Singer.builder()
                .name("夜行")
                .nameEn("Yexing")
                .description("低沉磁性的男声，擅长摇滚和流行")
                .voiceType("男声")
                .voiceStyle("摇滚")
                .voiceCharacter("磁性,低沉,有力")
                .suitableGenres("摇滚,流行,电子")
                .vocalRangeLow("E2").vocalRangeHigh("A4")
                .primaryLanguage("中文").supportedLanguages("中文,英文")
                .defaultVibratoDepth(55).defaultBreathiness(25).defaultTension(65)
                .avatarUrl("/avatars/yexing.png")
                .tags("摇滚,力量,磁性")
                .category("虚拟歌手").licenseType("免费")
                .sortOrder(3).enabled(true).build()
        );
        
        singerRepository.saveAll(singers);
        log.info("初始化了 {} 个默认AI歌手", singers.size());
    }
    
    private void initTemplates() {
        List<SongTemplate> templates = Arrays.asList(
            SongTemplate.builder()
                .name("甜蜜情歌")
                .description("适合表达爱情的甜蜜流行曲风")
                .category("流行")
                .iconEmoji("💕")
                .suggestedBpm(85)
                .suggestedKey("C大调")
                .moodKeywords("甜蜜,浪漫,温柔,幸福")
                .stylePrompt("写一首甜蜜的情歌，表达对爱人的深情，旋律优美动人")
                .structureTemplate("verse,chorus,verse,chorus,bridge,chorus")
                .exampleArtists("周杰伦,林俊杰,邓紫棋")
                .sortOrder(1).enabled(true).build(),
            SongTemplate.builder()
                .name("励志摇滚")
                .description("充满力量感的励志摇滚风格")
                .category("摇滚")
                .iconEmoji("🔥")
                .suggestedBpm(140)
                .suggestedKey("E大调")
                .moodKeywords("热血,励志,激情,奋斗")
                .stylePrompt("写一首充满力量的励志摇滚歌曲，鼓励人们勇敢追梦")
                .structureTemplate("intro,verse,chorus,verse,chorus,solo,chorus,outro")
                .exampleArtists("五月天,信乐团,Beyond")
                .sortOrder(2).enabled(true).build(),
            SongTemplate.builder()
                .name("古风仙侠")
                .description("中国风古典仙侠曲风")
                .category("古风")
                .iconEmoji("🏯")
                .suggestedBpm(75)
                .suggestedKey("A小调")
                .moodKeywords("古典,仙侠,江湖,侠骨柔情")
                .stylePrompt("写一首古风仙侠歌曲，描绘江湖儿女情长，意境悠远")
                .structureTemplate("intro,verse,verse,chorus,verse,chorus,outro")
                .exampleArtists("河图,银临,双笙")
                .sortOrder(3).enabled(true).build()
        );
        
        templateRepository.saveAll(templates);
        log.info("初始化了 {} 个歌曲模板", templates.size());
    }
    
    /**
     * 初始化演唱配置预设
     */
    private void initSingingConfigs() {
        if (singingConfigRepository.count() > 0) {
            log.info("演唱配置已存在，跳过初始化");
            return;
        }
        
        // 1. 标准流行风格
        singingConfigRepository.save(SingingConfig.builder()
                .name("标准流行")
                .nameEn("Standard Pop")
                .description("适合大多数流行歌曲的均衡配置")
                .category("流行")
                .useCase("通用流行歌曲、抒情歌曲")
                .defaultBpm(120)
                .timeSignature("4/4")
                .swingFeel(20)
                .autoBreath(true)
                .breathStrength(50)
                .baseVolume(70)
                .dynamicsMin(40)
                .dynamicsMax(100)
                .attackSpeed(30)
                .releaseSpeed(40)
                .autoDynamics(true)
                .accentStrength(60)
                .articulationClarity(70)
                .legatoAmount(60)
                .consonantStrength(50)
                .vowelLength(50)
                .endingStyle("natural")
                .pronunciationStyle("standard")
                .pitchShift(0)
                .portamentoEnabled(true)
                .portamentoTime(80)
                .pitchCorrection(50)
                .vibratoDepth(50)
                .vibratoRate(50)
                .vibratoDelay(200)
                .autoVibrato(true)
                .breathiness(30)
                .tension(50)
                .brightness(50)
                .genderFactor(50)
                .resonanceType("mixed")
                .reverbAmount(30)
                .reverbType("room")
                .isPreset(true)
                .enabled(true)
                .sortOrder(1)
                .build());
        
        // 2. 深情抒情风格
        singingConfigRepository.save(SingingConfig.builder()
                .name("深情抒情")
                .nameEn("Emotional Ballad")
                .description("适合慢节奏情歌，强调情感表达")
                .category("抒情")
                .useCase("情歌、慢摇、治愈系歌曲")
                .defaultBpm(70)
                .timeSignature("4/4")
                .swingFeel(10)
                .autoBreath(true)
                .breathStrength(60)
                .baseVolume(65)
                .dynamicsMin(30)
                .dynamicsMax(90)
                .attackSpeed(40)
                .releaseSpeed(50)
                .autoDynamics(true)
                .accentStrength(40)
                .articulationClarity(65)
                .legatoAmount(80)
                .consonantStrength(40)
                .vowelLength(70)
                .endingStyle("fadeout")
                .pronunciationStyle("soft")
                .pitchShift(0)
                .portamentoEnabled(true)
                .portamentoTime(120)
                .pitchCorrection(40)
                .vibratoDepth(60)
                .vibratoRate(40)
                .vibratoDelay(300)
                .autoVibrato(true)
                .breathiness(45)
                .tension(35)
                .brightness(40)
                .genderFactor(50)
                .resonanceType("head")
                .reverbAmount(45)
                .reverbType("hall")
                .isPreset(true)
                .enabled(true)
                .sortOrder(2)
                .build());
        
        // 3. 摇滚力量风格
        singingConfigRepository.save(SingingConfig.builder()
                .name("摇滚力量")
                .nameEn("Rock Power")
                .description("适合摇滚歌曲，强调力量和爆发力")
                .category("摇滚")
                .useCase("摇滚、金属、朋克风格歌曲")
                .defaultBpm(140)
                .timeSignature("4/4")
                .swingFeel(5)
                .autoBreath(true)
                .breathStrength(40)
                .baseVolume(85)
                .dynamicsMin(60)
                .dynamicsMax(100)
                .attackSpeed(15)
                .releaseSpeed(25)
                .autoDynamics(true)
                .accentStrength(80)
                .articulationClarity(80)
                .legatoAmount(30)
                .consonantStrength(70)
                .vowelLength(40)
                .endingStyle("cutoff")
                .pronunciationStyle("strong")
                .pitchShift(0)
                .portamentoEnabled(false)
                .portamentoTime(50)
                .pitchCorrection(60)
                .vibratoDepth(30)
                .vibratoRate(60)
                .vibratoDelay(100)
                .autoVibrato(false)
                .breathiness(15)
                .tension(80)
                .brightness(70)
                .genderFactor(60)
                .resonanceType("chest")
                .reverbAmount(20)
                .reverbType("room")
                .isPreset(true)
                .enabled(true)
                .sortOrder(3)
                .build());
        
        // 4. 古风戏腔风格
        singingConfigRepository.save(SingingConfig.builder()
                .name("古风戏腔")
                .nameEn("Chinese Traditional")
                .description("适合古风歌曲，带有戏曲元素")
                .category("古风")
                .useCase("古风、国风、戏腔类歌曲")
                .defaultBpm(80)
                .timeSignature("4/4")
                .swingFeel(30)
                .autoBreath(true)
                .breathStrength(55)
                .baseVolume(70)
                .dynamicsMin(35)
                .dynamicsMax(95)
                .attackSpeed(35)
                .releaseSpeed(45)
                .autoDynamics(true)
                .accentStrength(55)
                .articulationClarity(75)
                .legatoAmount(70)
                .consonantStrength(55)
                .vowelLength(60)
                .endingStyle("natural")
                .pronunciationStyle("standard")
                .pitchShift(0)
                .portamentoEnabled(true)
                .portamentoTime(150)
                .portamentoRange(3)
                .pitchCorrection(35)
                .vibratoDepth(70)
                .vibratoRate(35)
                .vibratoDelay(150)
                .autoVibrato(true)
                .breathiness(35)
                .tension(45)
                .brightness(55)
                .genderFactor(45)
                .resonanceType("head")
                .nasality(40)
                .reverbAmount(35)
                .reverbType("hall")
                .isPreset(true)
                .enabled(true)
                .sortOrder(4)
                .build());
        
        // 5. 电子舞曲风格
        singingConfigRepository.save(SingingConfig.builder()
                .name("电子舞曲")
                .nameEn("EDM/Electronic")
                .description("适合电子音乐，强调节奏感和现代感")
                .category("电子")
                .useCase("EDM、House、Trance等电子音乐")
                .defaultBpm(128)
                .timeSignature("4/4")
                .swingFeel(0)
                .autoBreath(false)
                .breathStrength(30)
                .baseVolume(75)
                .dynamicsMin(50)
                .dynamicsMax(100)
                .attackSpeed(20)
                .releaseSpeed(30)
                .autoDynamics(false)
                .accentStrength(70)
                .articulationClarity(85)
                .legatoAmount(40)
                .consonantStrength(60)
                .vowelLength(45)
                .endingStyle("cutoff")
                .pronunciationStyle("strong")
                .pitchShift(0)
                .portamentoEnabled(false)
                .portamentoTime(30)
                .pitchCorrection(80)
                .vibratoDepth(20)
                .vibratoRate(70)
                .vibratoDelay(100)
                .autoVibrato(false)
                .breathiness(20)
                .tension(60)
                .brightness(75)
                .genderFactor(50)
                .resonanceType("chest")
                .reverbAmount(25)
                .reverbType("plate")
                .chorusAmount(30)
                .isPreset(true)
                .enabled(true)
                .sortOrder(5)
                .build());
        
        // 6. R&B灵魂风格
        singingConfigRepository.save(SingingConfig.builder()
                .name("R&B灵魂")
                .nameEn("R&B Soul")
                .description("适合R&B风格，强调律动和转音")
                .category("R&B")
                .useCase("R&B、Soul、Neo-Soul风格歌曲")
                .defaultBpm(90)
                .timeSignature("4/4")
                .swingFeel(40)
                .autoBreath(true)
                .breathStrength(55)
                .baseVolume(70)
                .dynamicsMin(35)
                .dynamicsMax(95)
                .attackSpeed(35)
                .releaseSpeed(40)
                .autoDynamics(true)
                .accentStrength(55)
                .articulationClarity(65)
                .legatoAmount(75)
                .consonantStrength(45)
                .vowelLength(65)
                .endingStyle("natural")
                .pronunciationStyle("soft")
                .pitchShift(0)
                .portamentoEnabled(true)
                .portamentoTime(100)
                .portamentoRange(4)
                .pitchCorrection(30)
                .pitchDrift(30)
                .vibratoDepth(55)
                .vibratoRate(45)
                .vibratoDelay(250)
                .autoVibrato(true)
                .breathiness(40)
                .tension(40)
                .brightness(45)
                .genderFactor(50)
                .resonanceType("mixed")
                .reverbAmount(35)
                .reverbType("plate")
                .isPreset(true)
                .enabled(true)
                .sortOrder(6)
                .build());
        
        // 7. 民谣清新风格
        singingConfigRepository.save(SingingConfig.builder()
                .name("民谣清新")
                .nameEn("Folk Acoustic")
                .description("适合民谣风格，清新自然")
                .category("民谣")
                .useCase("民谣、校园歌曲、轻音乐")
                .defaultBpm(100)
                .timeSignature("4/4")
                .swingFeel(15)
                .autoBreath(true)
                .breathStrength(45)
                .baseVolume(65)
                .dynamicsMin(40)
                .dynamicsMax(85)
                .attackSpeed(35)
                .releaseSpeed(45)
                .autoDynamics(true)
                .accentStrength(45)
                .articulationClarity(75)
                .legatoAmount(55)
                .consonantStrength(50)
                .vowelLength(50)
                .endingStyle("natural")
                .pronunciationStyle("standard")
                .pitchShift(0)
                .portamentoEnabled(true)
                .portamentoTime(70)
                .pitchCorrection(45)
                .vibratoDepth(40)
                .vibratoRate(50)
                .vibratoDelay(250)
                .autoVibrato(true)
                .breathiness(35)
                .tension(40)
                .brightness(50)
                .genderFactor(50)
                .resonanceType("mixed")
                .reverbAmount(25)
                .reverbType("room")
                .isPreset(true)
                .enabled(true)
                .sortOrder(7)
                .build());
        
        log.info("初始化了 7 个演唱配置预设");
    }
    
    private void initDemoSong() {
        List<Singer> singers = singerRepository.findAll();
        if (singers.isEmpty()) return;
        
        Singer singer = singers.get(0);
        List<SingingTechnique> techniques = techniqueRepository.findAll();
        List<Emotion> emotions = emotionRepository.findAll();
        
        if (techniques.isEmpty() || emotions.isEmpty()) return;
        
        Song song = Song.builder()
                .title("星空下的约定")
                .lyrics("繁星点点照亮夜空\n我们许下最美的梦\n" +
                        "无论未来有多远\n这份约定永不变\n\n" +
                        "时光匆匆如流水\n带不走我们的回忆\n" +
                        "在这片星空之下\n我们的心紧紧相依")
                .musicStyle("流行")
                .bpm(90)
                .keySignature("C大调")
                .durationSeconds(240)
                .isGenerated(false)
                .singer(singer)
                .build();
        
        Song savedSong = songRepository.save(song);
        
        List<MusicSegment> segments = Arrays.asList(
            MusicSegment.builder()
                .song(savedSong)
                .segmentOrder(1)
                .segmentType("主歌")
                .startTime(0.0)
                .endTime(30.0)
                .lyrics("繁星点点照亮夜空\n我们许下最美的梦")
                .technique(techniques.get(0))
                .emotion(emotions.size() > 3 ? emotions.get(3) : emotions.get(0))
                .volumeLevel(90)
                .build(),
            MusicSegment.builder()
                .song(savedSong)
                .segmentOrder(2)
                .segmentType("副歌")
                .startTime(30.0)
                .endTime(60.0)
                .lyrics("无论未来有多远\n这份约定永不变")
                .technique(techniques.size() > 2 ? techniques.get(2) : techniques.get(0))
                .emotion(emotions.size() > 1 ? emotions.get(1) : emotions.get(0))
                .volumeLevel(100)
                .build()
        );
        
        segmentRepository.saveAll(segments);
        log.info("初始化了示例歌曲: {}", song.getTitle());
    }
}
