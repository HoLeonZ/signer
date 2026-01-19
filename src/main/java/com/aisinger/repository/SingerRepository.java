package com.aisinger.repository;

import com.aisinger.entity.Singer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SingerRepository extends JpaRepository<Singer, Long> {
    
    List<Singer> findByEnabledTrueOrderBySortOrderAsc();
    
    List<Singer> findByVoiceTypeAndEnabledTrue(String voiceType);
    
    List<Singer> findByVoiceStyleAndEnabledTrue(String voiceStyle);
}
