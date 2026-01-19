package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.entity.SingingTechnique;
import com.aisinger.service.TechniqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/techniques")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TechniqueController {
    
    private final TechniqueService techniqueService;
    
    @GetMapping
    public ApiResponse<List<SingingTechnique>> getAllTechniques() {
        return ApiResponse.success(techniqueService.getAllTechniques());
    }
    
    @GetMapping("/all")
    public ApiResponse<List<SingingTechnique>> getAllTechniquesIncludeDisabled() {
        return ApiResponse.success(techniqueService.getAllTechniquesIncludeDisabled());
    }
    
    @GetMapping("/{id}")
    public ApiResponse<SingingTechnique> getTechniqueById(@PathVariable Long id) {
        return techniqueService.getTechniqueById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("技巧不存在"));
    }
    
    @GetMapping("/category/{category}")
    public ApiResponse<List<SingingTechnique>> getTechniquesByCategory(@PathVariable String category) {
        return ApiResponse.success(techniqueService.getTechniquesByCategory(category));
    }
    
    @GetMapping("/difficulty/{maxLevel}")
    public ApiResponse<List<SingingTechnique>> getTechniquesByMaxDifficulty(@PathVariable Integer maxLevel) {
        return ApiResponse.success(techniqueService.getTechniquesByMaxDifficulty(maxLevel));
    }
    
    @PostMapping
    public ApiResponse<SingingTechnique> createTechnique(@RequestBody SingingTechnique technique) {
        return ApiResponse.success("技巧创建成功", techniqueService.createTechnique(technique));
    }
    
    @PutMapping("/{id}")
    public ApiResponse<SingingTechnique> updateTechnique(@PathVariable Long id, @RequestBody SingingTechnique technique) {
        return ApiResponse.success("技巧更新成功", techniqueService.updateTechnique(id, technique));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTechnique(@PathVariable Long id) {
        techniqueService.deleteTechnique(id);
        return ApiResponse.success("技巧删除成功", null);
    }
}
