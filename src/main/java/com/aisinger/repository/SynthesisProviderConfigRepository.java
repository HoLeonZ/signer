package com.aisinger.repository;

import com.aisinger.entity.SynthesisProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SynthesisProviderConfigRepository extends JpaRepository<SynthesisProviderConfig, Long> {
    
    Optional<SynthesisProviderConfig> findByProvider(String provider);
    
    List<SynthesisProviderConfig> findByEnabledTrueOrderBySortOrderAsc();
    
    List<SynthesisProviderConfig> findByServiceTypeOrderBySortOrderAsc(String serviceType);
    
    List<SynthesisProviderConfig> findByProviderTypeAndEnabledTrueOrderBySortOrderAsc(String providerType);
    
    Optional<SynthesisProviderConfig> findByIsActiveTrue();
    
    List<SynthesisProviderConfig> findAllByOrderBySortOrderAsc();
}
