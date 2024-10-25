package com.sparta.settlementprogram.settlement.entity;

import com.sparta.settlementprogram.streaming.entity.Videos;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class VideoStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Videos video; // 비디오 엔티티와 관계 설정

    @Column(name = "view_count", nullable = false)
    private Long viewCount; // 조회수

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate; // 날짜(일 단위로 조회수 저장)
}
