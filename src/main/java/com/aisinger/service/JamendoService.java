package com.aisinger.service;

import com.aisinger.config.JamendoProperties;
import com.aisinger.dto.JamendoResponse;
import com.aisinger.dto.JamendoSearchRequest;
import com.aisinger.dto.JamendoTrackDTO;
import com.aisinger.entity.JamendoConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Jamendo音乐库服务
 * 配置优先级：数据库配置 > YAML配置
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JamendoService {
    
    private final JamendoProperties properties; // YAML兜底配置
    @Lazy
    private final JamendoConfigService jamendoConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    /**
     * 获取有效配置（数据库优先，YAML兜底）
     */
    private JamendoConfig getEffectiveConfig() {
        return jamendoConfigService.getConfig();
    }
    
    /**
     * 搜索歌曲
     */
    public List<JamendoTrackDTO> searchTracks(JamendoSearchRequest request) {
        JamendoConfig config = getEffectiveConfig();
        
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            log.warn("Jamendo服务未启用");
            return Collections.emptyList();
        }
        
        String clientId = config.getClientId();
        if (clientId == null || clientId.isEmpty()) {
            log.warn("Jamendo Client ID未配置");
            return Collections.emptyList();
        }
        
        try {
            int defaultPageSize = config.getDefaultPageSize() != null ? config.getDefaultPageSize() : 20;
            String audioFormat = config.getAudioFormat() != null ? config.getAudioFormat() : "mp32";
            String apiUrl = config.getApiUrl() != null ? config.getApiUrl() : "https://api.jamendo.com/v3.0";
            
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(apiUrl + "/tracks/")
                    .queryParam("client_id", clientId)
                    .queryParam("format", "json")
                    .queryParam("limit", request.getLimit() != null ? request.getLimit() : defaultPageSize)
                    .queryParam("offset", request.getOffset() != null ? request.getOffset() : 0)
                    .queryParam("audioformat", audioFormat);
            
            // 添加搜索关键词
            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                builder.queryParam("search", request.getSearch());
            }
            
            // 添加艺术家过滤
            if (request.getArtistName() != null && !request.getArtistName().isEmpty()) {
                builder.queryParam("artist_name", request.getArtistName());
            }
            
            // 添加标签过滤
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                builder.queryParam("tags", request.getTags());
            }
            
            // 添加情绪过滤
            if (request.getMood() != null && !request.getMood().isEmpty()) {
                builder.queryParam("fuzzytags", request.getMood());
            }
            
            // 添加速度过滤
            if (request.getSpeed() != null && !request.getSpeed().isEmpty()) {
                builder.queryParam("speed", request.getSpeed());
            }
            
            // 添加人声/纯音乐过滤
            if (request.getVocalInstrumental() != null && !request.getVocalInstrumental().isEmpty()) {
                builder.queryParam("vocalinstrumental", request.getVocalInstrumental());
            }
            
            // 添加语言过滤
            if (request.getLang() != null && !request.getLang().isEmpty()) {
                builder.queryParam("lang", request.getLang());
            }
            
            // 是否只显示可商用
            boolean commercialOnly = Boolean.TRUE.equals(config.getCommercialOnly());
            if (Boolean.TRUE.equals(request.getProbackground()) || commercialOnly) {
                builder.queryParam("probackground", "true");
            }
            
            // 是否包含音乐信息
            if (Boolean.TRUE.equals(request.getIncludeMusicinfo())) {
                builder.queryParam("include", "musicinfo");
            }
            
            // 是否包含歌词
            if (Boolean.TRUE.equals(request.getIncludeLyrics())) {
                builder.queryParam("include", "lyrics");
            }
            
            // 排序
            if (request.getOrderBy() != null && !request.getOrderBy().isEmpty()) {
                builder.queryParam("order", request.getOrderBy());
            }
            
            String url = builder.build().toUriString();
            log.debug("Jamendo API请求: {}", url);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JamendoResponse<JamendoTrackDTO> jamendoResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<JamendoResponse<JamendoTrackDTO>>() {}
                );
                
                if ("success".equals(jamendoResponse.getHeaders().getStatus())) {
                    log.info("Jamendo搜索成功，返回 {} 首歌曲", jamendoResponse.getResults().size());
                    return jamendoResponse.getResults();
                } else {
                    log.error("Jamendo API错误: {}", jamendoResponse.getHeaders().getErrorMessage());
                    return Collections.emptyList();
                }
            } else {
                log.error("Jamendo API请求失败，状态码: {}", response.statusCode());
                return Collections.emptyList();
            }
            
        } catch (Exception e) {
            log.error("Jamendo API调用异常", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取热门歌曲
     */
    public List<JamendoTrackDTO> getPopularTracks(int limit, int offset) {
        JamendoSearchRequest request = JamendoSearchRequest.builder()
                .limit(limit)
                .offset(offset)
                .orderBy("popularity_total")
                .includeMusicinfo(true)
                .build();
        return searchTracks(request);
    }
    
    /**
     * 获取最新歌曲
     */
    public List<JamendoTrackDTO> getLatestTracks(int limit, int offset) {
        JamendoSearchRequest request = JamendoSearchRequest.builder()
                .limit(limit)
                .offset(offset)
                .orderBy("releasedate_desc")
                .includeMusicinfo(true)
                .build();
        return searchTracks(request);
    }
    
    /**
     * 按流派获取歌曲
     */
    public List<JamendoTrackDTO> getTracksByGenre(String genre, int limit, int offset) {
        JamendoSearchRequest request = JamendoSearchRequest.builder()
                .tags(genre)
                .limit(limit)
                .offset(offset)
                .orderBy("popularity_total")
                .includeMusicinfo(true)
                .build();
        return searchTracks(request);
    }
    
    /**
     * 按情绪获取歌曲
     */
    public List<JamendoTrackDTO> getTracksByMood(String mood, int limit, int offset) {
        JamendoSearchRequest request = JamendoSearchRequest.builder()
                .mood(mood)
                .limit(limit)
                .offset(offset)
                .orderBy("popularity_total")
                .includeMusicinfo(true)
                .build();
        return searchTracks(request);
    }
    
    /**
     * 获取单首歌曲详情
     */
    public JamendoTrackDTO getTrackById(String trackId) {
        JamendoConfig config = getEffectiveConfig();
        
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            log.warn("Jamendo服务未启用");
            return null;
        }
        
        String clientId = config.getClientId();
        if (clientId == null || clientId.isEmpty()) {
            log.warn("Jamendo Client ID未配置");
            return null;
        }
        
        try {
            String apiUrl = config.getApiUrl() != null ? config.getApiUrl() : "https://api.jamendo.com/v3.0";
            String audioFormat = config.getAudioFormat() != null ? config.getAudioFormat() : "mp32";
            
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiUrl + "/tracks/")
                    .queryParam("client_id", clientId)
                    .queryParam("format", "json")
                    .queryParam("id", trackId)
                    .queryParam("include", "musicinfo+lyrics")
                    .queryParam("audioformat", audioFormat)
                    .build()
                    .toUriString();
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JamendoResponse<JamendoTrackDTO> jamendoResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<JamendoResponse<JamendoTrackDTO>>() {}
                );
                
                if ("success".equals(jamendoResponse.getHeaders().getStatus()) 
                        && !jamendoResponse.getResults().isEmpty()) {
                    return jamendoResponse.getResults().get(0);
                }
            }
            return null;
            
        } catch (Exception e) {
            log.error("获取歌曲详情异常", e);
            return null;
        }
    }
    
    /**
     * 获取可用的流派列表
     */
    public List<String> getAvailableGenres() {
        return List.of(
            "pop", "rock", "electronic", "hiphop", "jazz", 
            "classical", "ambient", "folk", "metal", "punk",
            "reggae", "blues", "country", "soul", "rnb",
            "latin", "world", "soundtrack", "indie", "alternative"
        );
    }
    
    /**
     * 获取可用的情绪列表
     */
    public List<String> getAvailableMoods() {
        return List.of(
            "happy", "sad", "energetic", "relaxing", "romantic",
            "melancholic", "uplifting", "dark", "peaceful", "aggressive",
            "dreamy", "epic", "funky", "groovy", "inspiring"
        );
    }
    
    /**
     * 检查Jamendo服务状态
     */
    public Map<String, Object> getServiceStatus() {
        JamendoConfig config = getEffectiveConfig();
        String clientId = config.getClientId();
        
        return Map.of(
            "enabled", Boolean.TRUE.equals(config.getEnabled()),
            "clientIdConfigured", clientId != null && !clientId.isEmpty() && !clientId.contains("your-"),
            "apiUrl", config.getApiUrl() != null ? config.getApiUrl() : "https://api.jamendo.com/v3.0",
            "audioFormat", config.getAudioFormat() != null ? config.getAudioFormat() : "mp32",
            "commercialOnly", Boolean.TRUE.equals(config.getCommercialOnly()),
            "configSource", config.getId() != null ? "database" : "yaml"
        );
    }
}
