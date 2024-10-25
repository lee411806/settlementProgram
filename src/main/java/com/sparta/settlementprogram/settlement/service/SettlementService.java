package com.sparta.settlementprogram.settlement.service;

import com.sparta.settlementprogram.settlement.dto.SettlementDto;
import com.sparta.settlementprogram.settlement.entity.VideoSettlement;
import com.sparta.settlementprogram.settlement.repository.VideoSettlementRepository;
import com.sparta.settlementprogram.streaming.entity.Videos;
import com.sparta.settlementprogram.streaming.repository.VideoViewHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {

    private final VideoViewHistoryRepository videoViewHistoryRepository;

    private final VideoSettlementRepository videoSettlementRepository;

    public void statistics(String date) {

        if (date.equals("day")) {
            videoViewHistoryRepository.findTop5VideosByViewsToday();
        } else if (date.equals("week")) {
            videoViewHistoryRepository.findTop5VideosByViewsThisWeek();
        } else if (date.equals("month")) {
            videoViewHistoryRepository.findTop5VideosByViewsThisMonth();
        }

    }

    public void settlement(SettlementDto settlementDto) {
        String date = settlementDto.getDate();
        int currentPosition = settlementDto.getCurrentPosition();
        LocalDate playedDate = settlementDto.getPlayedDate();
        LocalDate startDate, endDate;
        Long videoView, adVideoView;
        long videoRevenue, adRevenue, totalRevenue;

        if (date.equals("day")) {
            videoView = videoViewHistoryRepository.findDailyViewCount(playedDate);
            adVideoView = videoViewHistoryRepository.findDailyAdViewCount(playedDate);
            videoRevenue = calculateVideoAmount(videoView);
            adRevenue = calculateAdAmount(adVideoView, currentPosition);
            totalRevenue = videoRevenue + adRevenue;
            startDate = endDate = playedDate;
        } else if (date.equals("week")) {
            startDate = playedDate.with(DayOfWeek.MONDAY);
            endDate = playedDate.with(DayOfWeek.SUNDAY);
            videoView = videoViewHistoryRepository.findWeeklyViewCount(startDate, endDate);
            adVideoView = videoViewHistoryRepository.findWeeklyAdViewCount(startDate, endDate);
            videoRevenue = calculateVideoAmount(videoView);
            adRevenue = calculateAdAmount(adVideoView, currentPosition);
            totalRevenue = videoRevenue + adRevenue;
        } else if (date.equals("month")) {
            startDate = playedDate.with(TemporalAdjusters.firstDayOfMonth());
            endDate = playedDate.with(TemporalAdjusters.lastDayOfMonth());
            videoView = videoViewHistoryRepository.findMonthlyViewCount(startDate, endDate);
            adVideoView = videoViewHistoryRepository.findMonthlyAdViewCount(startDate, endDate);
            videoRevenue = calculateVideoAmount(videoView);
            adRevenue = calculateAdAmount(adVideoView, currentPosition);
            totalRevenue = videoRevenue + adRevenue;
        } else {
            return; // 예외 처리 추가 가능
        }


        // 정산 데이터 저장
        VideoSettlement settlement = new VideoSettlement();
        settlement.setVideo(settlementDto.getVideo());
        settlement.setPeriodType(date);
        settlement.setStartDate(startDate);
        settlement.setEndDate(endDate);
        settlement.setVideoViewCount(videoView);
        settlement.setAdViewCount(adVideoView);
        settlement.setVideoRevenue(videoRevenue);
        settlement.setAdRevenue(adRevenue);
        settlement.setTotalRevenue(totalRevenue);

        videoSettlementRepository.save(settlement);
    }

    public void viewSettlement(Videos video, String date) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today;

        if (date.equals("day")) {
            // 하루 단위: startDate와 endDate를 동일하게
            startDate = endDate = today;

        } else if (date.equals("week")) {
            // 주 단위: 해당 날짜가 포함된 주의 월요일 ~ 일요일
            startDate = today.with(DayOfWeek.MONDAY);
            endDate = today.with(DayOfWeek.SUNDAY);

        } else if (date.equals("month")) {
            // 월 단위: 해당 날짜가 포함된 달의 1일 ~ 말일
            startDate = today.with(TemporalAdjusters.firstDayOfMonth());
            endDate = today.with(TemporalAdjusters.lastDayOfMonth());
        }

        // Repository를 통해 조회
        List<VideoSettlement> settlements = videoSettlementRepository.findByVideoAndDateRange(video, startDate, endDate);
        settlements.forEach(System.out::println); // 결과 출력 (테스트 용도)
    }


    private long calculateVideoAmount(long viewCount) {
        long amount = 0;

        if (viewCount > 1_000_000) {
            amount += (viewCount - 1_000_000) * 1.5;
            viewCount = 1_000_000;
        }
        if (viewCount > 500_000) {
            amount += (viewCount - 500_000) * 1.3;
            viewCount = 500_000;
        }
        if (viewCount > 100_000) {
            amount += (viewCount - 100_000) * 1.1;
            viewCount = 100_000;
        }
        amount += viewCount * 1.0;

        return (long) Math.floor(amount);
    }

    private long calculateAdAmount(long viewCount, int videoDurationInMinutes) {
        long amount = 0;
        int adCount = videoDurationInMinutes / 5;  // 5분당 1개 광고

        if (viewCount > 1_000_000) {
            amount += (viewCount - 1_000_000) * 20 * adCount;
            viewCount = 1_000_000;
        }
        if (viewCount > 500_000) {
            amount += (viewCount - 500_000) * 15 * adCount;
            viewCount = 500_000;
        }
        if (viewCount > 100_000) {
            amount += (viewCount - 100_000) * 12 * adCount;
            viewCount = 100_000;
        }
        amount += viewCount * 10 * adCount;

        return (long) Math.floor(amount);
    }


}
