package com.aisinger.repository;

import com.aisinger.entity.SingingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SingingConfigRepository extends JpaRepository<SingingConfig, Long> {
    
    /**
     * 查找所有启用的配置（按排序）
     */
    List<SingingConfig> findByEnabledTrueOrderBySortOrderAsc();
    
    /**
     * 根据分类查找
     */
    List<SingingConfig> findByCategoryAndEnabledTrueOrderBySortOrderAsc(String category);
    
    /**
     * 查找系统预设
     */
    List<SingingConfig> findByIsPresetTrueAndEnabledTrueOrderBySortOrderAsc();
    
    /**
     * 查找用户自定义配置
     */
    List<SingingConfig> findByIsPresetFalseAndEnabledTrueOrderBySortOrderAsc();
    
    /**
     * 按使用次数排序（热门）
     */
    List<SingingConfig> findByEnabledTrueOrderByUseCountDesc();
    
    /**
     * 根据名称模糊搜索
     */
    List<SingingConfig> findByNameContainingAndEnabledTrue(String keyword);
}
