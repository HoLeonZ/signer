package com.aisinger.controller;

import com.aisinger.dto.ApiResponse;
import com.aisinger.entity.Project;
import com.aisinger.repository.ProjectRepository;
import com.aisinger.repository.SingerRepository;
import com.aisinger.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {
    
    private final ProjectRepository projectRepository;
    private final SingerRepository singerRepository;
    private final SongRepository songRepository;
    
    @GetMapping
    public ApiResponse<List<Project>> getAllProjects() {
        return ApiResponse.success(projectRepository.findAllByOrderByUpdatedAtDesc());
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Project> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("项目不存在"));
    }
    
    @PostMapping
    public ApiResponse<Project> createProject(@RequestBody Map<String, Object> request) {
        Project project = Project.builder()
                .name((String) request.get("name"))
                .description((String) request.get("description"))
                .configJson(request.get("config") != null ? request.get("config").toString() : null)
                .status("draft")
                .build();
        
        if (request.get("singerId") != null) {
            Long singerId = Long.valueOf(request.get("singerId").toString());
            singerRepository.findById(singerId).ifPresent(project::setSinger);
        }
        
        if (request.get("songId") != null) {
            Long songId = Long.valueOf(request.get("songId").toString());
            songRepository.findById(songId).ifPresent(project::setSong);
        }
        
        return ApiResponse.success("项目创建成功", projectRepository.save(project));
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Project> updateProject(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (request.get("name") != null) {
                        project.setName((String) request.get("name"));
                    }
                    if (request.get("description") != null) {
                        project.setDescription((String) request.get("description"));
                    }
                    if (request.get("config") != null) {
                        project.setConfigJson(request.get("config").toString());
                    }
                    if (request.get("status") != null) {
                        project.setStatus((String) request.get("status"));
                    }
                    return ApiResponse.success("项目更新成功", projectRepository.save(project));
                })
                .orElse(ApiResponse.error("项目不存在"));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
        return ApiResponse.success("项目删除成功", null);
    }
    
    @PostMapping("/{id}/export")
    public ApiResponse<Map<String, Object>> exportProject(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    // 返回可导出的项目数据
                    Map<String, Object> exportData = Map.of(
                        "projectName", project.getName(),
                        "singer", project.getSinger() != null ? project.getSinger().getName() : "",
                        "song", project.getSong() != null ? project.getSong().getTitle() : "",
                        "config", project.getConfigJson() != null ? project.getConfigJson() : "{}",
                        "exportedAt", System.currentTimeMillis()
                    );
                    return ApiResponse.success("导出成功", exportData);
                })
                .orElse(ApiResponse.error("项目不存在"));
    }
}
