package com.sparta.settlementprogram.settlement.entity;

import com.sparta.settlementprogram.streaming.entity.Videos;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "video_settlement")
@Getter
@Setter
@NoArgsConstructor
public class VideoSettlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Videos video;

    private String periodType;           // 정산 기간 타입 ('day', 'week', 'month')
    private LocalDate startDate;          // 정산 시작일
    private LocalDate endDate;            // 정산 종료일
    private Long videoViewCount;          // 조회 수
    private Long adViewCount;             // 광고 조회 수
    private Long videoRevenue;            // 영상 정산 금액
    private Long adRevenue;               // 광고 정산 금액
    private Long totalRevenue;            // 총 정산 금액 (videoRevenue + adRevenue)

}
