package com.sparta.settlementprogram.streaming.repository;

import com.sparta.settlementprogram.entity.User;
import com.sparta.settlementprogram.streaming.entity.VideoViewHistory;
import com.sparta.settlementprogram.streaming.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoViewHistoryRepository extends JpaRepository<VideoViewHistory, Long> {
    Optional<VideoViewHistory> findByUserIdAndVideoId(Long userId, Long videoId);

    Optional<VideoViewHistory> findByUserAndVideo(User user, Videos video);
}
