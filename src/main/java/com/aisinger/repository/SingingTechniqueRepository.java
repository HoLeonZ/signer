package com.aisinger.repository;

import com.aisinger.entity.SingingTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SingingTechniqueRepository extends JpaRepository<SingingTechnique, Long> {
    
    List<SingingTechnique> findByEnabledTrueOrderBySortOrderAsc();
    
    List<SingingTechnique> findByCategoryAndEnabledTrue(String category);
    
    List<SingingTechnique> findByDifficultyLevelLessThanEqualAndEnabledTrue(Integer maxLevel);
}
