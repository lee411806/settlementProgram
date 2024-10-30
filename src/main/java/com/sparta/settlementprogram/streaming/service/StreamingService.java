package com.sparta.settlementprogram.streaming.service;


import com.sparta.settlementprogram.streaming.entity.DailyVideoView;
import com.sparta.settlementprogram.streaming.repository.DailyVideoViewRepository;
import com.sparta.settlementprogram.user.entity.User;
import com.sparta.settlementprogram.user.jwt.JwtUtil;
import com.sparta.settlementprogram.user.repository.UserRepository;
import com.sparta.settlementprogram.streaming.dto.AdviewcountRequestDto;
import com.sparta.settlementprogram.streaming.entity.ReviewCountAuthentication;
import com.sparta.settlementprogram.streaming.entity.VideoViewHistory;
import com.sparta.settlementprogram.streaming.entity.Videos;
import com.sparta.settlementprogram.streaming.repository.ReviewCountAuthenticationRepository;
import com.sparta.settlementprogram.streaming.repository.VideoRepository;
import com.sparta.settlementprogram.streaming.repository.VideoViewHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class StreamingService {

    private final VideoViewHistoryRepository videoViewHistoryRepository;

    private final UserRepository userRepository;

    private final VideoRepository videoRepository;

    private final ReviewCountAuthenticationRepository reviewCountAuthenticationRepository;

    private final DailyVideoViewRepository dailyVideoViewRepository;

    private final JwtUtil jwtUtil;

    //동영상 재생서비스
    //실제로 로그인 하고 play 날려서 jwtToken, ipAddress 잡히는 지 볼 것
    //반환 값 : 현재 시청중인 동영상 시점
    public int play(Long userId, Long videoId, HttpServletRequest httpServletRequest) {
        String jwtToken = jwtUtil.getJwtFromHeader(httpServletRequest);
        String ipAddress = getClientIp(httpServletRequest);

        // 어뷰징 체크
        if (isAbusiveRequest(jwtToken, ipAddress)) {
            // 마지막 액션 시점 반환 why? -> 어뷰징 체크되면 조회수는 안올라가지만 최근 시점에서 재생해야 되기 때문에
            return getLastActionTime(jwtToken, ipAddress);
        }else{

            // 재생 위치 조회 및 조회수 증가
            Optional<VideoViewHistory> historyOpt = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId);
            if (historyOpt.isPresent()) {
                //시청 기록이 있을 때 조회수 증가
                incrementViewCount(videoId);

                // 시청 기록이 있는 경우: 마지막 재생 위치를 반환
                return historyOpt.get().getCurrentPosition();
            } else {
                // 시청 기록이 없는 경우: 새로운 시청 기록을 생성하여 반환
                int currentPosition = createNewHistory(userId, videoId);

                // 시청 기록이 없을 때 조회수 증가
                incrementViewCount(videoId);

                return currentPosition;
            }

        }


    }

    // 어뷰징 방지 메서드
    // 테스트 완
    public boolean isAbusiveRequest(String jwtToken, String ipAddress) {
        Optional<ReviewCountAuthentication> reviewCountAuthentication = reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress);
        // 초 단위로 계산
        int currentTimeInSeconds = (int) (System.currentTimeMillis() / 1000);
        if (reviewCountAuthentication.isPresent()) {
            ReviewCountAuthentication authLog = reviewCountAuthentication.get();
            return (currentTimeInSeconds - authLog.getLastActionTime()) <= 30;
        } else {
            return false;
        }
    }

    // 마지막 액션 시간 반환 메서드
    public int getLastActionTime(String jwtToken, String ipAddress) {
        Optional<ReviewCountAuthentication> reviewCountAuthentication = reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress);

        if (reviewCountAuthentication.isPresent()) {
            // 값이 있는 경우: 마지막 액션 시간을 반환
            return reviewCountAuthentication.get().getLastActionTime();
        } else {
            // 값이 없는 경우: 기본값 0을 반환
            return 0;
        }
    }

    // 조회 이력이 없는 경우 새로운 기록 생성
    public int createNewHistory(Long userId, Long videoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        Videos video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid video ID: " + videoId));

        VideoViewHistory newHistory = new VideoViewHistory();
        newHistory.setUser(user);
        newHistory.setVideo(video);
        newHistory.setCurrentPosition(0);
        newHistory.setLastPlayedDate(LocalDateTime.now());
        newHistory.setVideoViewCount(1L);

        videoViewHistoryRepository.save(newHistory);
        return 0; // 새로 생성되었으므로 0초 반환
    }

    // 조회수 증가 메서드
    public void incrementViewCount(Long videoId) {
        Videos video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 비디오입니다."));

        LocalDate today = LocalDate.now();

        // DailyVideoView에서 오늘 날짜로 된 기록이 있는지 확인
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoAndDate(video, today)
                .orElseGet(() -> new DailyVideoView(video, today)); // 없으면 새로운 레코드 생성

        //조회수 1증가
        //incrementViewCount는 재귀가 아니라 엔티티에 있는 메서드(자바 프로그램 짜듯이 엔티티에 메서드 넣어놈)
        dailyVideoView.incrementViewCount();

        // 저장
        dailyVideoViewRepository.save(dailyVideoView);


    }

    //정지 메서드
    public void pause(Long userId, Long videoId, int currentPosition, HttpServletRequest httpServletRequest) {
        //비디오 중지 시점 저장
        VideoViewHistory history = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseThrow(() -> new EntityNotFoundException("No history found for this user and video"));

        history.setLastPlayedDate(LocalDateTime.now());
        history.setCurrentPosition(currentPosition);

        // JWT 토큰과 IP 주소를 가져와서 어뷰징 방지 엔티티에 저장
        String jwtToken = jwtUtil.getJwtFromHeader(httpServletRequest);
        String ipAddress = getClientIp(httpServletRequest);


        //비디오 총 재생시간 증가
        Videos video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 비디오입니다."));
        LocalDate today = LocalDate.now();
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoAndDate(video, today)
                .orElseThrow(() -> new IllegalStateException("시간, 비디오 총 재생 시간 테이블이 없습니다. Video ID: " + videoId + ", Date: " + today));

        // 조회수 증가 로직
        dailyVideoView.increasePlaytime(currentPosition);
        dailyVideoViewRepository.save(dailyVideoView);

        // 어뷰징 방지로 현재 중지 시점 리뷰조회수인증 엔티티에 저장
        // ip는 HttpServletRequest로 , 인증키는 Jwt 토큰에서 받을 수 있음
        ReviewCountAuthentication reviewCountAuthentication = new ReviewCountAuthentication();
        reviewCountAuthentication.setLastActionTime(currentPosition);
        reviewCountAuthentication.setJwtToken(jwtToken);
        reviewCountAuthentication.setIpAddress(ipAddress);

        reviewCountAuthenticationRepository.save(reviewCountAuthentication); //어뷰징 방지 테이블 업데이트
        videoViewHistoryRepository.save(history); // 시청 기록도 업데이트
    }

    //광고 조회수 메서드
    //비디오를 시청하다 광고시점이 오면 client가 server에 요청
    public void adviewcount(AdviewcountRequestDto adviewcountRequestDto) {
        Videos video = videoRepository.findById(adviewcountRequestDto.getVideoId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 비디오입니다."));
        LocalDate today = LocalDate.now();

        // DailyVideoView에서 오늘 날짜와 해당 비디오 ID로 된 기록이 있는지 확인
        DailyVideoView dailyVideoView = dailyVideoViewRepository.findByVideoAndDate(video, today)
                .orElseGet(() -> new DailyVideoView(video, today)); // 없으면 새로운 레코드 생성

        // 광고 시청 횟수 증가
        dailyVideoView.incrementAdViewCount();

        // 변경 사항 저장
        dailyVideoViewRepository.save(dailyVideoView);

    }


    // IP 주소를 가져오기 위한 메서드
    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");  // 프록시를 통해 요청이 올 경우
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();  // 마지막으로 직접 클라이언트의 IP를 가져옴
        }
        return ipAddress;
    }
}

