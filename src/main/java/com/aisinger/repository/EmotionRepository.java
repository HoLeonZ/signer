package com.aisinger.repository;

import com.aisinger.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    
    List<Emotion> findByEnabledTrueOrderBySortOrderAsc();
    
    List<Emotion> findByCategoryAndEnabledTrue(String category);
}
