package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.entity.SongTemplate;
import com.aisinger.repository.SongTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 歌曲模板控制器
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TemplateController {
    
    private final SongTemplateRepository templateRepository;
    
    @GetMapping
    public ApiResponse<List<SongTemplate>> getAllTemplates() {
        return ApiResponse.success(templateRepository.findByEnabledTrueOrderBySortOrderAsc());
    }
    
    @GetMapping("/popular")
    public ApiResponse<List<SongTemplate>> getPopularTemplates() {
        return ApiResponse.success(templateRepository.findTop10ByEnabledTrueOrderByUseCountDesc());
    }
    
    @GetMapping("/category/{category}")
    public ApiResponse<List<SongTemplate>> getTemplatesByCategory(@PathVariable String category) {
        return ApiResponse.success(templateRepository.findByCategoryAndEnabledTrue(category));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<SongTemplate> getTemplateById(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("模板不存在"));
    }
    
    @PostMapping("/{id}/use")
    public ApiResponse<SongTemplate> useTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(template -> {
                    template.setUseCount(template.getUseCount() + 1);
                    return ApiResponse.success(templateRepository.save(template));
                })
                .orElse(ApiResponse.error("模板不存在"));
    }
}
