package com.sparta.settlementprogram.streaming.repository;

import com.sparta.settlementprogram.streaming.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Videos, Long> {
}
