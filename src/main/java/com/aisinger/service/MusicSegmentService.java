package com.aisinger.service;

import com.aisinger.dto.SegmentUpdateRequest;
import com.aisinger.entity.MusicSegment;
import com.aisinger.repository.EmotionRepository;
import com.aisinger.repository.MusicSegmentRepository;
import com.aisinger.repository.SingingTechniqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MusicSegmentService {
    
    private final MusicSegmentRepository segmentRepository;
    private final SingingTechniqueRepository techniqueRepository;
    private final EmotionRepository emotionRepository;
    
    public List<MusicSegment> getSegmentsBySong(Long songId) {
        return segmentRepository.findBySongIdOrderBySegmentOrderAsc(songId);
    }
    
    public Optional<MusicSegment> getSegmentById(Long id) {
        return segmentRepository.findById(id);
    }
    
    @Transactional
    public MusicSegment updateSegment(Long id, SegmentUpdateRequest request) {
        MusicSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("片段不存在: " + id));
        
        if (request.getTechniqueId() != null) {
            techniqueRepository.findById(request.getTechniqueId())
                    .ifPresent(segment::setTechnique);
        }
        
        if (request.getEmotionId() != null) {
            emotionRepository.findById(request.getEmotionId())
                    .ifPresent(segment::setEmotion);
        }
        
        if (request.getVolumeLevel() != null) {
            segment.setVolumeLevel(request.getVolumeLevel());
        }
        
        if (request.getPitchShift() != null) {
            segment.setPitchShift(request.getPitchShift());
        }
        
        if (request.getNotes() != null) {
            segment.setNotes(request.getNotes());
        }
        
        return segmentRepository.save(segment);
    }
    
    @Transactional
    public List<MusicSegment> batchUpdateSegments(List<Long> ids, Long techniqueId, Long emotionId) {
        List<MusicSegment> segments = segmentRepository.findAllById(ids);
        
        for (MusicSegment segment : segments) {
            if (techniqueId != null) {
                techniqueRepository.findById(techniqueId)
                        .ifPresent(segment::setTechnique);
            }
            if (emotionId != null) {
                emotionRepository.findById(emotionId)
                        .ifPresent(segment::setEmotion);
            }
        }
        
        return segmentRepository.saveAll(segments);
    }
}
