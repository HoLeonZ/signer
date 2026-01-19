package com.aisinger.service;

import com.aisinger.dto.SongCreateRequest;
import com.aisinger.entity.*;
import com.aisinger.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongService {
    
    private final SongRepository songRepository;
    private final SingerRepository singerRepository;
    private final MusicSegmentRepository segmentRepository;
    private final SingingTechniqueRepository techniqueRepository;
    private final EmotionRepository emotionRepository;
    private final SingingConfigRepository singingConfigRepository;
    
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }
    
    public Optional<Song> getSongById(Long id) {
        return Optional.ofNullable(songRepository.findByIdWithSegments(id));
    }
    
    public List<Song> getSongsBySinger(Long singerId) {
        return songRepository.findBySingerId(singerId);
    }
    
    public List<Song> getSongsByStyle(String style) {
        return songRepository.findByMusicStyle(style);
    }
    
    public List<Song> searchSongs(String keyword) {
        return songRepository.findByTitleContainingIgnoreCase(keyword);
    }
    
    @Transactional
    public Song createSong(SongCreateRequest request) {
        Song song = Song.builder()
                .title(request.getTitle())
                .lyrics(request.getLyrics())
                .musicStyle(request.getMusicStyle())
                .bpm(request.getBpm())
                .keySignature(request.getKeySignature())
                .isGenerated(request.getIsGenerated() != null ? request.getIsGenerated() : false)
                // 外部数据源字段
                .externalSource(request.getExternalSource())
                .externalId(request.getExternalId())
                .externalUrl(request.getExternalUrl())
                .audioUrl(request.getAudioUrl())
                .coverUrl(request.getCoverUrl())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .duration(request.getDuration())
                .license(request.getLicense())
                .build();
        
        if (request.getSingerId() != null) {
            Singer singer = singerRepository.findById(request.getSingerId())
                    .orElseThrow(() -> new RuntimeException("歌手不存在"));
            song.setSinger(singer);
        }
        
        // 关联演唱配置
        if (request.getSingingConfigId() != null) {
            SingingConfig singingConfig = singingConfigRepository.findById(request.getSingingConfigId())
                    .orElse(null);
            if (singingConfig != null) {
                song.setSingingConfig(singingConfig);
                log.info("歌曲 [{}] 关联演唱配置 [{}]", request.getTitle(), singingConfig.getName());
            }
        }
        
        Song savedSong = songRepository.save(song);
        
        // 创建片段
        if (request.getSegments() != null && !request.getSegments().isEmpty()) {
            for (SongCreateRequest.SegmentConfig segConfig : request.getSegments()) {
                MusicSegment segment = MusicSegment.builder()
                        .song(savedSong)
                        .segmentOrder(segConfig.getSegmentOrder())
                        .segmentType(segConfig.getSegmentType())
                        .startTime(segConfig.getStartTime())
                        .endTime(segConfig.getEndTime())
                        .lyrics(segConfig.getLyrics())
                        .volumeLevel(segConfig.getVolumeLevel() != null ? segConfig.getVolumeLevel() : 100)
                        .pitchShift(segConfig.getPitchShift() != null ? segConfig.getPitchShift() : 0)
                        .build();
                
                if (segConfig.getTechniqueId() != null) {
                    techniqueRepository.findById(segConfig.getTechniqueId())
                            .ifPresent(segment::setTechnique);
                }
                
                if (segConfig.getEmotionId() != null) {
                    emotionRepository.findById(segConfig.getEmotionId())
                            .ifPresent(segment::setEmotion);
                }
                
                segmentRepository.save(segment);
            }
        }
        
        return songRepository.findByIdWithSegments(savedSong.getId());
    }
    
    @Transactional
    public Song createGeneratedSong(String title, String lyrics, String style, Integer bpm, Long singerId) {
        Song song = Song.builder()
                .title(title)
                .lyrics(lyrics)
                .musicStyle(style)
                .bpm(bpm)
                .isGenerated(true)
                .build();
        
        if (singerId != null) {
            singerRepository.findById(singerId).ifPresent(song::setSinger);
        }
        
        return songRepository.save(song);
    }
    
    @Transactional
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }
}
