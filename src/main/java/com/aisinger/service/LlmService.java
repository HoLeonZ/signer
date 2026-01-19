package com.aisinger.service;

import com.aisinger.config.LlmProperties;
import com.aisinger.dto.LyricsGenerateRequest;
import com.aisinger.dto.LyricsGenerateResponse;
import com.aisinger.dto.SingerGenerateRequest;
import com.aisinger.dto.SingerGenerateResponse;
import com.aisinger.entity.LlmConfig;
import com.aisinger.entity.Singer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM服务 - 支持多个LLM提供商（通义千问、OpenAI、Gemini）
 * 配置优先级：数据库配置 > YAML配置
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LlmService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final LlmProperties llmProperties;
    @Lazy
    private final LlmConfigService llmConfigService;
    
    /**
     * 生成歌词
     */
    public LyricsGenerateResponse generateLyrics(LyricsGenerateRequest request) {
        String prompt = buildPrompt(request);
        
        try {
            String response = callLlm(prompt);
            return parseLyricsResponse(response, request);
        } catch (Exception e) {
            log.error("LLM调用失败: {}", e.getMessage(), e);
            return generateFallbackLyrics(request);
        }
    }
    
    /**
     * 根据配置调用相应的LLM
     * 优先使用数据库配置，回退到YAML配置
     */
    private String callLlm(String prompt) {
        LlmConfig activeConfig = llmConfigService.getActiveConfig();
        String provider = activeConfig.getProvider().toLowerCase();
        log.info("使用LLM提供商: {} ({})", activeConfig.getDisplayName(), provider);
        
        return switch (provider) {
            case "openai" -> callOpenAIWithConfig(prompt, activeConfig);
            case "gemini" -> callGeminiWithConfig(prompt, activeConfig);
            default -> callQwenWithConfig(prompt, activeConfig);
        };
    }
    
    /**
     * 调用阿里通义千问API（使用数据库配置）
     */
    private String callQwenWithConfig(String prompt, LlmConfig config) {
        log.info("调用通义千问API，模型: {}", config.getModelName());
        
        Double temperature = config.getTemperature() != null ? config.getTemperature() : 0.8;
        Integer maxTokens = config.getMaxTokens() != null ? config.getMaxTokens() : 2000;
        
        Map<String, Object> requestBody = Map.of(
            "model", config.getModelName(),
            "messages", List.of(
                Map.of("role", "system", "content", "你是一位专业的词曲创作人，擅长创作各种风格的歌词。请用中文回复。"),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", temperature,
            "max_tokens", maxTokens
        );
        
        int timeout = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 60;
        return callOpenAICompatibleApi(config.getApiUrl(), config.getApiKey(), requestBody, timeout);
    }
    
    /**
     * 调用阿里通义千问API（使用YAML配置 - 兜底）
     */
    private String callQwen(String prompt) {
        LlmProperties.ProviderConfig config = llmProperties.getQwen();
        log.info("调用通义千问API（YAML配置），模型: {}", config.getModel());
        
        Map<String, Object> requestBody = Map.of(
            "model", config.getModel(),
            "messages", List.of(
                Map.of("role", "system", "content", "你是一位专业的词曲创作人，擅长创作各种风格的歌词。请用中文回复。"),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.8,
            "max_tokens", 2000
        );
        
        return callOpenAICompatibleApi(config.getApiUrl(), config.getApiKey(), requestBody, 60);
    }
    
    /**
     * 调用OpenAI API（使用数据库配置）
     */
    private String callOpenAIWithConfig(String prompt, LlmConfig config) {
        log.info("调用OpenAI API，模型: {}", config.getModelName());
        
        Double temperature = config.getTemperature() != null ? config.getTemperature() : 0.8;
        Integer maxTokens = config.getMaxTokens() != null ? config.getMaxTokens() : 2000;
        
        Map<String, Object> requestBody = Map.of(
            "model", config.getModelName(),
            "messages", List.of(
                Map.of("role", "system", "content", "你是一位专业的词曲创作人，擅长创作各种风格的歌词。请用中文回复。"),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", temperature,
            "max_tokens", maxTokens
        );
        
        int timeout = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 60;
        return callOpenAICompatibleApi(config.getApiUrl(), config.getApiKey(), requestBody, timeout);
    }
    
    /**
     * 调用OpenAI API（使用YAML配置 - 兜底）
     */
    private String callOpenAI(String prompt) {
        LlmProperties.ProviderConfig config = llmProperties.getOpenai();
        log.info("调用OpenAI API（YAML配置），模型: {}", config.getModel());
        
        Map<String, Object> requestBody = Map.of(
            "model", config.getModel(),
            "messages", List.of(
                Map.of("role", "system", "content", "你是一位专业的词曲创作人，擅长创作各种风格的歌词。请用中文回复。"),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.8,
            "max_tokens", 2000
        );
        
        return callOpenAICompatibleApi(config.getApiUrl(), config.getApiKey(), requestBody, 60);
    }
    
    /**
     * 调用OpenAI兼容API（适用于OpenAI和通义千问）
     */
    private String callOpenAICompatibleApi(String apiUrl, String apiKey, Map<String, Object> requestBody, int timeoutSeconds) {
        WebClient webClient = webClientBuilder.build();
        
        String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();
        
        log.debug("API响应: {}", response);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").path("message").asText();
                log.error("API错误: {}", errorMsg);
                throw new RuntimeException("API错误: " + errorMsg);
            }
            
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("解析API响应失败", e);
            throw new RuntimeException("解析API响应失败", e);
        }
    }
    
    /**
     * 调用Google Gemini API（使用数据库配置）
     */
    private String callGeminiWithConfig(String prompt, LlmConfig config) {
        log.info("调用Gemini API，模型: {}", config.getModelName());
        
        Double temperature = config.getTemperature() != null ? config.getTemperature() : 0.8;
        Integer maxTokens = config.getMaxTokens() != null ? config.getMaxTokens() : 2000;
        int timeout = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 60;
        
        // Gemini API格式
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", "你是一位专业的词曲创作人，擅长创作各种风格的歌词。请用中文回复。\n\n" + prompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature", temperature,
                "maxOutputTokens", maxTokens
            )
        );
        
        // Gemini API URL格式: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
        String apiUrl = config.getApiUrl() + "/" + config.getModelName() + ":generateContent?key=" + config.getApiKey();
        
        WebClient webClient = webClientBuilder.build();
        
        String response = webClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeout))
                .block();
        
        log.debug("Gemini响应: {}", response);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").path("message").asText();
                log.error("Gemini API错误: {}", errorMsg);
                throw new RuntimeException("Gemini API错误: " + errorMsg);
            }
            
            // Gemini响应格式
            return jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("解析Gemini响应失败", e);
            throw new RuntimeException("解析Gemini响应失败", e);
        }
    }
    
    /**
     * 调用Google Gemini API（使用YAML配置 - 兜底）
     */
    private String callGemini(String prompt) {
        LlmProperties.ProviderConfig config = llmProperties.getGemini();
        log.info("调用Gemini API（YAML配置），模型: {}", config.getModel());
        
        // Gemini API格式
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", "你是一位专业的词曲创作人，擅长创作各种风格的歌词。请用中文回复。\n\n" + prompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature", 0.8,
                "maxOutputTokens", 2000
            )
        );
        
        // Gemini API URL格式: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
        String apiUrl = config.getApiUrl() + "/" + config.getModel() + ":generateContent?key=" + config.getApiKey();
        
        WebClient webClient = webClientBuilder.build();
        
        String response = webClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        log.debug("Gemini响应: {}", response);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").path("message").asText();
                log.error("Gemini API错误: {}", errorMsg);
                throw new RuntimeException("Gemini API错误: " + errorMsg);
            }
            
            // Gemini响应格式
            return jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("解析Gemini响应失败", e);
            throw new RuntimeException("解析Gemini响应失败", e);
        }
    }
    
    /**
     * 构建歌词生成提示词
     */
    private String buildPrompt(LyricsGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的词曲创作人。请根据以下要求创作一首歌曲的歌词：\n\n");
        
        if (request.getTheme() != null) {
            prompt.append("主题：").append(request.getTheme()).append("\n");
        }
        if (request.getMood() != null) {
            prompt.append("情绪基调：").append(request.getMood()).append("\n");
        }
        if (request.getStyle() != null) {
            prompt.append("音乐风格：").append(request.getStyle()).append("\n");
        }
        if (request.getLanguage() != null) {
            prompt.append("语言：").append(request.getLanguage()).append("\n");
        }
        if (request.getVerseCount() != null) {
            prompt.append("主歌段落数：").append(request.getVerseCount()).append("\n");
        }
        if (request.getHasChorus() != null && request.getHasChorus()) {
            prompt.append("需要包含副歌\n");
        }
        if (request.getKeywords() != null) {
            prompt.append("关键词：").append(request.getKeywords()).append("\n");
        }
        if (request.getAdditionalPrompt() != null) {
            prompt.append("额外要求：").append(request.getAdditionalPrompt()).append("\n");
        }
        
        prompt.append("\n请按以下格式输出：\n");
        prompt.append("【标题】歌曲标题\n");
        prompt.append("【风格建议】音乐风格\n");
        prompt.append("【BPM建议】节拍速度（数字）\n");
        prompt.append("【前奏】(可选的前奏描述)\n");
        prompt.append("【主歌1】歌词内容\n");
        prompt.append("【副歌】歌词内容\n");
        prompt.append("【主歌2】歌词内容\n");
        prompt.append("【桥段】(可选)\n");
        prompt.append("【尾声】(可选)\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析LLM返回的歌词响应
     */
    private LyricsGenerateResponse parseLyricsResponse(String response, LyricsGenerateRequest request) {
        List<LyricsGenerateResponse.LyricsSection> sections = new ArrayList<>();
        String title = "未命名歌曲";
        String suggestedStyle = request.getStyle();
        Integer suggestedBpm = 120;
        StringBuilder fullLyrics = new StringBuilder();
        
        // 解析标题
        Pattern titlePattern = Pattern.compile("【标题】(.+?)(?=\\n|$)");
        Matcher titleMatcher = titlePattern.matcher(response);
        if (titleMatcher.find()) {
            title = titleMatcher.group(1).trim();
        }
        
        // 解析BPM
        Pattern bpmPattern = Pattern.compile("【BPM建议】(\\d+)");
        Matcher bpmMatcher = bpmPattern.matcher(response);
        if (bpmMatcher.find()) {
            suggestedBpm = Integer.parseInt(bpmMatcher.group(1));
        }
        
        // 解析风格
        Pattern stylePattern = Pattern.compile("【风格建议】(.+?)(?=\\n|$)");
        Matcher styleMatcher = stylePattern.matcher(response);
        if (styleMatcher.find()) {
            suggestedStyle = styleMatcher.group(1).trim();
        }
        
        // 解析各段落
        String[] sectionTypes = {"前奏", "主歌1", "主歌2", "主歌3", "副歌", "桥段", "尾声"};
        String[] sectionTypeEn = {"intro", "verse", "verse", "verse", "chorus", "bridge", "outro"};
        String[] suggestedEmotions = {"期待", "叙述", "深情", "激昂", "高潮", "转折", "回味"};
        String[] suggestedTechniques = {"轻声", "自然", "颤音", "假声", "混声", "转音", "气声"};
        
        for (int i = 0; i < sectionTypes.length; i++) {
            Pattern sectionPattern = Pattern.compile("【" + sectionTypes[i] + "】([\\s\\S]*?)(?=【|$)");
            Matcher sectionMatcher = sectionPattern.matcher(response);
            if (sectionMatcher.find()) {
                String content = sectionMatcher.group(1).trim();
                if (!content.isEmpty()) {
                    sections.add(LyricsGenerateResponse.LyricsSection.builder()
                            .type(sectionTypeEn[i])
                            .content(content)
                            .suggestedEmotion(suggestedEmotions[i])
                            .suggestedTechnique(suggestedTechniques[i])
                            .build());
                    fullLyrics.append(content).append("\n\n");
                }
            }
        }
        
        return LyricsGenerateResponse.builder()
                .title(title)
                .fullLyrics(fullLyrics.toString().trim())
                .sections(sections)
                .suggestedStyle(suggestedStyle)
                .suggestedBpm(suggestedBpm)
                .build();
    }
    
    /**
     * 生成备用歌词（当LLM调用失败时）
     */
    private LyricsGenerateResponse generateFallbackLyrics(LyricsGenerateRequest request) {
        List<LyricsGenerateResponse.LyricsSection> sections = new ArrayList<>();
        
        String theme = request.getTheme() != null ? request.getTheme() : "美好生活";
        String mood = request.getMood() != null ? request.getMood() : "愉快";
        
        sections.add(LyricsGenerateResponse.LyricsSection.builder()
                .type("verse")
                .content("阳光洒落在窗台\n" + theme + "悄悄走来\n微风轻轻吹过\n带着" + mood + "的期待")
                .suggestedEmotion("期待")
                .suggestedTechnique("自然")
                .build());
        
        sections.add(LyricsGenerateResponse.LyricsSection.builder()
                .type("chorus")
                .content("让我们一起歌唱\n唱出心中的梦想\n无论路有多远\n" + theme + "就在前方")
                .suggestedEmotion("激昂")
                .suggestedTechnique("混声")
                .build());
        
        sections.add(LyricsGenerateResponse.LyricsSection.builder()
                .type("verse")
                .content("星光点缀夜空\n" + mood + "充满心中\n每一个瞬间\n都值得珍重")
                .suggestedEmotion("深情")
                .suggestedTechnique("颤音")
                .build());
        
        sections.add(LyricsGenerateResponse.LyricsSection.builder()
                .type("outro")
                .content("就这样继续前行\n" + theme + "永远相随")
                .suggestedEmotion("回味")
                .suggestedTechnique("气声")
                .build());
        
        StringBuilder fullLyrics = new StringBuilder();
        for (LyricsGenerateResponse.LyricsSection section : sections) {
            fullLyrics.append(section.getContent()).append("\n\n");
        }
        
        return LyricsGenerateResponse.builder()
                .title("关于" + theme + "的歌")
                .fullLyrics(fullLyrics.toString().trim())
                .sections(sections)
                .suggestedStyle(request.getStyle() != null ? request.getStyle() : "流行")
                .suggestedBpm(120)
                .build();
    }
    
    /**
     * 获取当前使用的LLM提供商
     */
    public String getCurrentProvider() {
        LlmConfig activeConfig = llmConfigService.getActiveConfig();
        return activeConfig.getDisplayName() != null ? activeConfig.getDisplayName() : activeConfig.getProvider();
    }
    
    /**
     * AI一键生成歌手
     */
    public SingerGenerateResponse generateSinger(SingerGenerateRequest request) {
        String prompt = buildSingerPrompt(request);
        
        try {
            String response = callLlm(prompt);
            return parseSingerResponse(response, request);
        } catch (Exception e) {
            log.error("AI生成歌手失败: {}", e.getMessage(), e);
            return generateFallbackSinger(request);
        }
    }
    
    /**
     * 构建歌手生成的提示词
     */
    private String buildSingerPrompt(SingerGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的AI歌手设计师。请根据用户的描述，设计一个完整的AI歌手角色。\n\n");
        
        prompt.append("用户需求：").append(request.getPrompt()).append("\n");
        
        if (request.getReferenceArtist() != null && !request.getReferenceArtist().isEmpty()) {
            prompt.append("参考歌手风格：").append(request.getReferenceArtist()).append("\n");
        }
        
        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            prompt.append("主要语言：").append(request.getLanguage()).append("\n");
        }
        
        prompt.append("\n请严格按照以下JSON格式输出歌手配置（不要输出其他内容）：\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"name\": \"歌手中文名\",\n");
        prompt.append("  \"nameEn\": \"歌手英文名\",\n");
        prompt.append("  \"description\": \"歌手详细介绍（100-200字）\",\n");
        prompt.append("  \"voiceType\": \"男声/女声/中性/童声\",\n");
        prompt.append("  \"voiceStyle\": \"主要风格（如：流行、古风、摇滚、R&B、民谣、电子等）\",\n");
        prompt.append("  \"voiceCharacter\": \"声音特点（如：温暖、清澈、磁性、高亢、低沉、空灵、沙哑等）\",\n");
        prompt.append("  \"suitableGenres\": \"适合曲风（逗号分隔）\",\n");
        prompt.append("  \"vocalRangeLow\": \"最低音（如：A2、C3）\",\n");
        prompt.append("  \"vocalRangeHigh\": \"最高音（如：C5、E5）\",\n");
        prompt.append("  \"tessituraLow\": \"最佳音区下限\",\n");
        prompt.append("  \"tessituraHigh\": \"最佳音区上限\",\n");
        prompt.append("  \"supportedLanguages\": \"支持的语言（逗号分隔）\",\n");
        prompt.append("  \"primaryLanguage\": \"主要语言\",\n");
        prompt.append("  \"techniqueStrength\": \"擅长技巧（逗号分隔，如：颤音,假声,转音,气声）\",\n");
        prompt.append("  \"emotionStrength\": \"擅长情感（逗号分隔，如：深情,激昂,温柔,悲伤）\",\n");
        prompt.append("  \"breathStyle\": \"气息风格（自然/轻柔/有力）\",\n");
        prompt.append("  \"articulationStyle\": \"咬字风格（清晰/柔和/有力）\",\n");
        prompt.append("  \"defaultVibratoDepth\": 颤音深度数值(0-100),\n");
        prompt.append("  \"defaultVibratoRate\": 颤音速率数值(0-100),\n");
        prompt.append("  \"defaultBreathiness\": 气声程度数值(0-100),\n");
        prompt.append("  \"defaultTension\": 张力数值(0-100),\n");
        prompt.append("  \"defaultBrightness\": 明亮度数值(0-100),\n");
        prompt.append("  \"defaultGenderFactor\": 性别因子数值(0-100，0最女性化，100最男性化),\n");
        prompt.append("  \"tags\": \"标签（逗号分隔）\",\n");
        prompt.append("  \"category\": \"分类（原创/虚拟歌手/翻唱）\",\n");
        prompt.append("  \"designNotes\": \"设计理念说明\",\n");
        prompt.append("  \"recommendedGenres\": \"推荐歌曲风格\",\n");
        prompt.append("  \"recommendedSongTypes\": \"推荐演唱的歌曲类型\"\n");
        prompt.append("}\n");
        prompt.append("```\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析LLM返回的歌手配置
     */
    private SingerGenerateResponse parseSingerResponse(String response, SingerGenerateRequest request) {
        try {
            // 提取JSON部分
            String jsonContent = extractJson(response);
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            Singer singer = Singer.builder()
                    .name(getTextValue(jsonNode, "name", "AI歌手"))
                    .nameEn(getTextValue(jsonNode, "nameEn", "AI Singer"))
                    .description(getTextValue(jsonNode, "description", "由AI生成的虚拟歌手"))
                    .voiceType(getTextValue(jsonNode, "voiceType", "女声"))
                    .voiceStyle(getTextValue(jsonNode, "voiceStyle", "流行"))
                    .voiceCharacter(getTextValue(jsonNode, "voiceCharacter", "温暖"))
                    .suitableGenres(getTextValue(jsonNode, "suitableGenres", "流行,抒情"))
                    .vocalRangeLow(getTextValue(jsonNode, "vocalRangeLow", "C3"))
                    .vocalRangeHigh(getTextValue(jsonNode, "vocalRangeHigh", "C5"))
                    .tessituraLow(getTextValue(jsonNode, "tessituraLow", "E3"))
                    .tessituraHigh(getTextValue(jsonNode, "tessituraHigh", "A4"))
                    .supportedLanguages(getTextValue(jsonNode, "supportedLanguages", "中文"))
                    .primaryLanguage(getTextValue(jsonNode, "primaryLanguage", request.getLanguage() != null ? request.getLanguage() : "中文"))
                    .techniqueStrength(getTextValue(jsonNode, "techniqueStrength", "颤音,气声"))
                    .emotionStrength(getTextValue(jsonNode, "emotionStrength", "深情,温柔"))
                    .breathStyle(getTextValue(jsonNode, "breathStyle", "自然"))
                    .articulationStyle(getTextValue(jsonNode, "articulationStyle", "清晰"))
                    .defaultVibratoDepth(getIntValue(jsonNode, "defaultVibratoDepth", 50))
                    .defaultVibratoRate(getIntValue(jsonNode, "defaultVibratoRate", 50))
                    .defaultBreathiness(getIntValue(jsonNode, "defaultBreathiness", 30))
                    .defaultTension(getIntValue(jsonNode, "defaultTension", 50))
                    .defaultBrightness(getIntValue(jsonNode, "defaultBrightness", 50))
                    .defaultGenderFactor(getIntValue(jsonNode, "defaultGenderFactor", 50))
                    .tags(getTextValue(jsonNode, "tags", "AI生成"))
                    .category(getTextValue(jsonNode, "category", "虚拟歌手"))
                    .enabled(true)
                    .sortOrder(0)
                    .popularity(0)
                    .voiceEngine("mock")
                    .licenseType("免费")
                    .creator("AI生成")
                    .build();
            
            return SingerGenerateResponse.builder()
                    .singer(singer)
                    .designNotes(getTextValue(jsonNode, "designNotes", "根据用户需求AI生成"))
                    .recommendedGenres(getTextValue(jsonNode, "recommendedGenres", singer.getSuitableGenres()))
                    .recommendedSongTypes(getTextValue(jsonNode, "recommendedSongTypes", "抒情歌曲"))
                    .build();
                    
        } catch (Exception e) {
            log.error("解析歌手JSON失败: {}", e.getMessage(), e);
            return generateFallbackSinger(request);
        }
    }
    
    /**
     * 从响应中提取JSON
     */
    private String extractJson(String response) {
        // 尝试提取```json...```之间的内容
        Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 尝试提取```...```之间的内容
        pattern = Pattern.compile("```\\s*([\\s\\S]*?)```");
        matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 尝试提取{...}
        pattern = Pattern.compile("\\{[\\s\\S]*\\}");
        matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        return response;
    }
    
    /**
     * 安全获取文本值
     */
    private String getTextValue(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return defaultValue;
    }
    
    /**
     * 安全获取整数值
     */
    private Integer getIntValue(JsonNode node, String field, Integer defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            try {
                return node.get(field).asInt();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 生成备用歌手（当LLM调用失败时）
     */
    private SingerGenerateResponse generateFallbackSinger(SingerGenerateRequest request) {
        String prompt = request.getPrompt() != null ? request.getPrompt() : "";
        
        // 基于关键词简单推断
        boolean isMale = prompt.contains("男") || prompt.contains("男声") || prompt.contains("male");
        boolean isRock = prompt.contains("摇滚") || prompt.contains("rock");
        boolean isGentle = prompt.contains("温柔") || prompt.contains("gentle") || prompt.contains("柔");
        
        String voiceType = isMale ? "男声" : "女声";
        String voiceStyle = isRock ? "摇滚" : "流行";
        String voiceCharacter = isGentle ? "温暖" : (isRock ? "沙哑" : "清澈");
        int genderFactor = isMale ? 70 : 30;
        int tension = isRock ? 70 : 50;
        int breathiness = isGentle ? 40 : 25;
        
        Singer singer = Singer.builder()
                .name("梦想之声")
                .nameEn("Dream Voice")
                .description("由AI根据您的需求生成的虚拟歌手。" + (prompt.length() > 0 ? "基于描述：" + prompt : ""))
                .voiceType(voiceType)
                .voiceStyle(voiceStyle)
                .voiceCharacter(voiceCharacter)
                .suitableGenres(isRock ? "摇滚,流行,独立" : "流行,抒情,民谣")
                .vocalRangeLow(isMale ? "A2" : "C3")
                .vocalRangeHigh(isMale ? "A4" : "C5")
                .tessituraLow(isMale ? "C3" : "E3")
                .tessituraHigh(isMale ? "E4" : "A4")
                .supportedLanguages("中文,英文")
                .primaryLanguage(request.getLanguage() != null ? request.getLanguage() : "中文")
                .techniqueStrength("颤音,气声,转音")
                .emotionStrength("深情,激昂,温柔")
                .breathStyle(isGentle ? "轻柔" : "自然")
                .articulationStyle("清晰")
                .defaultVibratoDepth(50)
                .defaultVibratoRate(50)
                .defaultBreathiness(breathiness)
                .defaultTension(tension)
                .defaultBrightness(50)
                .defaultGenderFactor(genderFactor)
                .tags("AI生成,虚拟歌手")
                .category("虚拟歌手")
                .enabled(true)
                .sortOrder(0)
                .popularity(0)
                .voiceEngine("mock")
                .licenseType("免费")
                .creator("AI生成")
                .build();
        
        return SingerGenerateResponse.builder()
                .singer(singer)
                .designNotes("由于AI服务暂时不可用，系统根据关键词自动生成了基础配置，您可以在管理界面进行调整。")
                .recommendedGenres(singer.getSuitableGenres())
                .recommendedSongTypes("流行歌曲,抒情歌曲")
                .build();
    }
}
