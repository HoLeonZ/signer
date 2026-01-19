package com.aisinger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Jamendo API响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JamendoResponse<T> {
    
    /**
     * 响应头信息
     */
    private Headers headers;
    
    /**
     * 结果数据列表
     */
    private List<T> results;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Headers {
        /**
         * 状态: success, failed
         */
        private String status;
        
        /**
         * 状态码
         */
        private Integer code;
        
        /**
         * 错误信息
         */
        @JsonProperty("error_message")
        private String errorMessage;
        
        /**
         * 警告信息
         */
        private String warnings;
        
        /**
         * 结果总数
         */
        @JsonProperty("results_count")
        private Integer resultsCount;
    }
}
