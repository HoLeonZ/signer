package com.aisinger.repository;

import com.aisinger.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    
    List<Song> findBySingerId(Long singerId);
    
    List<Song> findByMusicStyle(String musicStyle);
    
    List<Song> findByIsGeneratedTrue();
    
    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.segments WHERE s.id = :id")
    Song findByIdWithSegments(Long id);
    
    List<Song> findByTitleContainingIgnoreCase(String title);
}
