package com.aisinger.repository;

import com.aisinger.entity.JamendoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JamendoConfigRepository extends JpaRepository<JamendoConfig, Long> {
    
    /**
     * 根据名称查找配置
     */
    Optional<JamendoConfig> findByName(String name);
    
    /**
     * 查找默认配置
     */
    default Optional<JamendoConfig> findDefault() {
        return findByName("default");
    }
}
