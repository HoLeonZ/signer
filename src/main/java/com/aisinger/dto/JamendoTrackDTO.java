package com.aisinger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Jamendo歌曲DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JamendoTrackDTO {
    
    /**
     * 歌曲ID
     */
    private String id;
    
    /**
     * 歌曲名称
     */
    private String name;
    
    /**
     * 时长（秒）
     */
    private Integer duration;
    
    /**
     * 艺术家ID
     */
    @JsonProperty("artist_id")
    private String artistId;
    
    /**
     * 艺术家名称
     */
    @JsonProperty("artist_name")
    private String artistName;
    
    /**
     * 专辑ID
     */
    @JsonProperty("album_id")
    private String albumId;
    
    /**
     * 专辑名称
     */
    @JsonProperty("album_name")
    private String albumName;
    
    /**
     * 许可证类型
     */
    @JsonProperty("license_ccurl")
    private String licenseCcurl;
    
    /**
     * 封面图片URL
     */
    private String image;
    
    /**
     * 音频URL
     */
    private String audio;
    
    /**
     * 音频下载URL
     */
    private String audiodownload;
    
    /**
     * 是否允许下载
     */
    @JsonProperty("audiodownload_allowed")
    private Boolean audiodownloadAllowed;
    
    /**
     * 发布日期
     */
    private String releasedate;
    
    /**
     * 分享URL
     */
    private String shareurl;
    
    /**
     * 短链接
     */
    private String shorturl;
    
    /**
     * 波形数据URL
     */
    private String waveform;
    
    /**
     * 位置/地区
     */
    private String position;
    
    /**
     * 音乐信息（包含标签、流派等）
     */
    private MusicInfo musicinfo;
    
    /**
     * 歌词
     */
    private String lyrics;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MusicInfo {
        /**
         * 标签列表
         */
        private Tags tags;
        
        /**
         * 乐器
         */
        private String lang;
        
        /**
         * 流派
         */
        private String genre;
        
        /**
         * 速度/节奏
         */
        private String speed;
        
        /**
         * 情绪
         */
        @JsonProperty("vocalinstrumental")
        private String vocalInstrumental;
        
        /**
         * 性别
         */
        private String gender;
        
        /**
         * 声学/电子
         */
        private String acousticelectric;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tags {
        private List<String> genres;
        private List<String> instruments;
        private List<String> vartags;
    }
}
