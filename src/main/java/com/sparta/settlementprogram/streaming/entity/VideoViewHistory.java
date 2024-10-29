package com.sparta.settlementprogram.streaming.entity;

import com.sparta.settlementprogram.user.entity.Timestamped;
import com.sparta.settlementprogram.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "videohistory")
@Getter
@Setter
public class VideoViewHistory extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Videos video;

    @ManyToOne
    @JoinColumn(name = "user")
    private User user;

    private Long videoViewCount;  // 일반 비디오 조회수
    private Long adViewCount;  // 광고 시청 횟수

    private LocalDateTime lastPlayedDate;
    private LocalDate playedDate;

    private Integer currentPosition;


}
