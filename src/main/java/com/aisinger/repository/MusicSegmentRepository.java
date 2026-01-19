package com.aisinger.repository;

import com.aisinger.entity.MusicSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicSegmentRepository extends JpaRepository<MusicSegment, Long> {
    
    List<MusicSegment> findBySongIdOrderBySegmentOrderAsc(Long songId);
    
    List<MusicSegment> findByTechniqueId(Long techniqueId);
    
    List<MusicSegment> findByEmotionId(Long emotionId);
}
