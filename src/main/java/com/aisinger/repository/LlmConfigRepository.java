package com.aisinger.repository;

import com.aisinger.entity.LlmConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LlmConfigRepository extends JpaRepository<LlmConfig, Long> {
    
    /**
     * 根据提供商标识查找配置
     */
    Optional<LlmConfig> findByProvider(String provider);
    
    /**
     * 查找当前激活的LLM配置
     */
    Optional<LlmConfig> findByIsActiveTrue();
    
    /**
     * 查找所有启用的LLM配置
     */
    List<LlmConfig> findByEnabledTrueOrderBySortOrderAsc();
    
    /**
     * 查找所有配置（按排序顺序）
     */
    List<LlmConfig> findAllByOrderBySortOrderAsc();
}
