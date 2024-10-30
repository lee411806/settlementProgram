package com.sparta.settlementprogram.streaming.service;

import com.sparta.settlementprogram.streaming.entity.ReviewCountAuthentication;
import com.sparta.settlementprogram.streaming.entity.VideoViewHistory;
import com.sparta.settlementprogram.streaming.entity.Videos;
import com.sparta.settlementprogram.streaming.repository.ReviewCountAuthenticationRepository;
import com.sparta.settlementprogram.streaming.repository.VideoRepository;
import com.sparta.settlementprogram.streaming.repository.VideoViewHistoryRepository;
import com.sparta.settlementprogram.user.entity.User;
import com.sparta.settlementprogram.user.jwt.JwtUtil;
import com.sparta.settlementprogram.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class StreamingServiceTest {

    @InjectMocks
    private StreamingService streamingService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private VideoViewHistoryRepository videoViewHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private ReviewCountAuthenticationRepository reviewCountAuthenticationRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);  // Mock 객체 초기화
    }


    /*
    given 준비(환경설정)
    when 실행 (테스트 동작 수행)
    then 검증
    */

    // jwtToken ipAddress 미리 설정,
    @Test
    @DisplayName("어뷰징 요청 감지 테스트")
    public void testIsAbusiveRequest() {
        // given: 테스트를 위한 mock 데이터와 조건을 설정
        String jwtToken = "mock-jwt";
        String ipAddress = "192.168.1.1";

        // 20초 전 요청이 있었던 것으로 설정
        ReviewCountAuthentication reviewCountAuthentication = new ReviewCountAuthentication();
        reviewCountAuthentication.setLastActionTime((int) (System.currentTimeMillis() / 1000) - 29);

        // findByJwtTokenAndIpAddress 호출 시 reviewCountAuthentication 반환하도록 설정
        //테스트가 서비스 코드의 흐름을 그대로 따르게 하기 위한 구색 맞추기 -> 실제 쿼리가 실행되지는 않음
        when(reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress))
                .thenReturn(Optional.of(reviewCountAuthentication));

        // when: 어뷰징 요청 감지 메서드를 호출
        boolean isAbusive = streamingService.isAbusiveRequest(jwtToken, ipAddress);

        // then: 어뷰징으로 감지되었는지 확인
        assertTrue(isAbusive, "어뷰징 테스트를 실패했습니다.");
        if (isAbusive) {
            System.out.println("어뷰징 테스트가 성공했습니다. 이건 어뷰징입니다."); // 성공 시 출력
        }
    }

    @Test
    @DisplayName("어뷰징 요청이 아닌 경우 테스트")
    public void testIsNotAbusiveRequest() {
        // given
        String jwtToken = "mock-jwt";
        String ipAddress = "192.168.1.1";

        // 40초 전 요청이 있었던 것으로 설정
        ReviewCountAuthentication authLog = new ReviewCountAuthentication();
        authLog.setLastActionTime((int) (System.currentTimeMillis() / 1000) - 31);

        when(reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress))
                .thenReturn(Optional.of(authLog));

        // when
        boolean isAbusive = streamingService.isAbusiveRequest(jwtToken, ipAddress);

        // then
        assertFalse(isAbusive, "어뷰징 테스트 실패.");
        if(!isAbusive) {
            System.out.println("어뷰징이 아닙니다.");
        }
    }


    @Test
    @DisplayName("비디오 재생 시간을 올바르게 반환 하는지 테스트")
    public void testGetLastActionTime() {
        String jwtToken = "mock-jwt";
        String ipAddress = "192.168.1.1";

        ReviewCountAuthentication reviewCountAuthentication = new ReviewCountAuthentication();
        reviewCountAuthentication.setLastActionTime(123456);

        when(reviewCountAuthenticationRepository.findByJwtTokenAndIpAddress(jwtToken, ipAddress))
                .thenReturn(Optional.of(reviewCountAuthentication));

        int lastActionTime = streamingService.getLastActionTime(jwtToken, ipAddress);
        assertEquals(123456, lastActionTime);
    }

    @Test
    @DisplayName("새로운 조회 기록이 올바르게 생성되었는지 확인")
    public void testCreateNewHistory() {
        Long userId = 1L;
        Long videoId = 1L;

        User user = new User();
        user.setId(userId);

        Videos video = new Videos();
        video.setId(videoId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        int result = streamingService.createNewHistory(userId, videoId);
        assertEquals(0, result);  // 새 기록이므로 0 반환

        verify(videoViewHistoryRepository).save(any(VideoViewHistory.class));
    }




}
