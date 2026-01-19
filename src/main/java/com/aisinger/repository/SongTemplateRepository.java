package com.aisinger.repository;

import com.aisinger.entity.SongTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongTemplateRepository extends JpaRepository<SongTemplate, Long> {
    
    List<SongTemplate> findByEnabledTrueOrderBySortOrderAsc();
    
    List<SongTemplate> findByCategoryAndEnabledTrue(String category);
    
    List<SongTemplate> findTop10ByEnabledTrueOrderByUseCountDesc();
}
