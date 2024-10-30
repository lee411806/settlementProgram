package com.sparta.settlementprogram.streaming.controller;

import com.sparta.settlementprogram.streaming.dto.AdviewcountRequestDto;
import com.sparta.settlementprogram.streaming.service.StreamingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class StreamingController {

    private final StreamingService streamingService;

    //조회 수 증가 후 현재 재생시점 반환
    @GetMapping("/users/{userId}/videos/{videoId}/play")
    public int play(@PathVariable Long userId, @PathVariable Long videoId , HttpServletRequest httpServletRequest) {
              return streamingService.play(userId,videoId,httpServletRequest);
    }

    // 현재 재생 시점 db에 저장
    @GetMapping("/users/{userId}/videos/{videoId}/pause")
    public void pause(@PathVariable Long userId,
                      @PathVariable Long videoId,
                      @RequestParam int currentPosition,
                      @RequestParam  HttpServletRequest httpServletRequest ) {

            streamingService.pause(userId,videoId,currentPosition,httpServletRequest);

    }

    //광고 조회수 증가
    @PostMapping("/adviewcount")
    public void adviewcount(@RequestBody AdviewcountRequestDto adviewcountRequestDto){
            streamingService.adviewcount(adviewcountRequestDto);
    }

}
