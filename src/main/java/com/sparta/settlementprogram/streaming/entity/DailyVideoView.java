package com.sparta.settlementprogram.streaming.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DailyVideoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Videos video; // 동영상 정보와 연관 관계

    @Column(name = "date", nullable = false)
    private LocalDate date; // 날짜별 조회수를 기록하기 위한 날짜

    @Column(name = "view_count")
    private Long viewCount = 0L; // 날짜별 조회수 초기값 0으로 설정

    @Column(name = "adview_count")
    private Long adViewCount = 0L; // 날짜별 광고 시청 횟수 초기값 0으로 설정

    @Column(name="playtime")
    private Long playTime = 0L;

    public DailyVideoView(Videos video, LocalDate today) {
        this.video = video;
        this.date = today;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount += 1;
    }


    public void incrementAdViewCount() {
        this.adViewCount += 1;
    }

    public void increasePlaytime(int currentposition) {
        this.playTime += currentposition;
    }
}
