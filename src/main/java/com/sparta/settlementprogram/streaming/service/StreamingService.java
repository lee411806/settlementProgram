package com.sparta.settlementprogram.streaming.service;


import com.sparta.settlementprogram.entity.User;
import com.sparta.settlementprogram.jwt.JwtUtil;
import com.sparta.settlementprogram.repository.UserRepository;
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

    private final JwtUtil jwtUtil;


    //실행 메서드
    public int play(Long userId, Long videoId, HttpServletRequest httpServletRequest) {

        // JWT 인증키와 IP 주소 추출
        String jwtToken = jwtUtil.getJwtFromHeader(httpServletRequest);
        String ipAddress = getClientIp(httpServletRequest); // IP 주소 가져오는 메서드 (위에서 구현한 방식 사용)

        // 어뷰징 방지 로그를 조회
        Optional<ReviewCountAuthentication> authLogOpt = reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress);

        // 현재 시간을 초 단위 Integer로 변환
        int currentTimeInSeconds = (int) (System.currentTimeMillis() / 1000);

        if (authLogOpt.isPresent()) {
            ReviewCountAuthentication authLog = authLogOpt.get();
            Integer lastActionTime = authLog.getLastActionTime();

            // 현재 시간과 마지막 액션 시간 차이를 초 단위로 계산
            if ((currentTimeInSeconds - lastActionTime) < 30) {
                // 30초 이내일 경우 조회수를 올리지 않음
                System.out.println("Abusive request detected. View count not increased.");
                return lastActionTime;  // 마지막 액션 시점 반환
            }
        }


        //처음 시작할때
        Optional<VideoViewHistory> history = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId);

        if (history.isPresent()) {
            // Optional에서 값을 꺼내와서 currentPosition을 가져옴
            int currentPosition = history.get().getCurrentPosition();


            return currentPosition; // 해당 시점을 반환
        } else {
            // 새로운 기록 생성 후 저장
            VideoViewHistory newHistory = new VideoViewHistory();
            // User와 Videos 객체를 생성하거나 조회해서 설정해야 함
            // Optional로 User 조회 후 처리
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));  // userId로 User 객체를 조회

            // Optional로 Videos 조회 후 처리 , findbyid는 optional 던진다.
            Videos video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid video ID: " + videoId));  // videoId로 Videos 객체를 조회

            newHistory.setUser(user);  // User 객체 설정
            newHistory.setVideo(video);  // Videos 객체 설정
            newHistory.setCurrentPosition(0);  // 처음 재생이므로 0초로 설정
            newHistory.setLastPlayedDate(LocalDateTime.now()); // 현재 날짜 저장
            newHistory.setVideoViewCount(
                    newHistory.getVideoViewCount() == null ? 1L : newHistory.getVideoViewCount() + 1L
            );
            //일반 date랑 localdateTime이랑 다른가? date -localdate로해주니 에러 해결했음


            // 새로운 기록 저장
            videoViewHistoryRepository.save(newHistory);

            // 처음 재생이므로 0 반환
            return 0;
        }

    }

    //정지 메서드
    public void pause(Long userId, Long videoId, int currentPosition) {

        VideoViewHistory history = videoViewHistoryRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseThrow(() -> new EntityNotFoundException("No history found for this user and video"));

        history.setLastPlayedDate(LocalDateTime.now());
        history.setCurrentPosition(currentPosition);

        // 어뷰징 방지로 현재 중지 시점 리뷰조회수인증 엔티티에 저장
        // ip는 HttpServletRequest로 , 인증키는 Jwt 토큰에서 받을 수 있음
        ReviewCountAuthentication reviewCountAuthentication = new ReviewCountAuthentication();
        reviewCountAuthentication.setLastActionTime(currentPosition);

        reviewCountAuthenticationRepository.save(reviewCountAuthentication);


    }

    //광고 조회수 메서드
    public void adviewcount(AdviewcountRequestDto adviewcountRequestDto) {


        // Optional로 User 조회 후 처리
        User user = userRepository.findById(adviewcountRequestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + adviewcountRequestDto.getUserId()));  // userId로 User 객체를 조회

        // Optional로 Videos 조회 후 처리 , findbyid는 optional 던진다.
        Videos video = videoRepository.findById(adviewcountRequestDto.getVideoId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid video ID: " + adviewcountRequestDto.getVideoId()));  // videoId로 Videos 객체를 조회

        // 시청 기록을 조회하고, 없으면 예외를 던짐
        VideoViewHistory history = videoViewHistoryRepository.findByUserAndVideo(user, video)
                .orElseThrow(() -> new EntityNotFoundException("Ad view history not found for this user and video"));

        history.setLastPlayedDate(LocalDateTime.now());
        // 광고 조회수 증가 로직을 간결하게 처리
        history.setAdViewCount(
                history.getAdViewCount() == null ? 1L : history.getAdViewCount() + 1L
        );

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
