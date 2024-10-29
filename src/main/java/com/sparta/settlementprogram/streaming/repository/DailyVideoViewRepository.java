package com.sparta.settlementprogram.streaming.repository;

import com.sparta.settlementprogram.streaming.entity.DailyVideoView;
import com.sparta.settlementprogram.streaming.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyVideoViewRepository extends JpaRepository<DailyVideoView, Long> {
    Optional<DailyVideoView> findByVideoAndDate(Videos video, LocalDate today);
}
