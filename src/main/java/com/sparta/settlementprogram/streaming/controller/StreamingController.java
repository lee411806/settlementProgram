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

    @GetMapping("/users/{userId}/videos/{videoId}/play")
    public int play(@PathVariable Long userId, @PathVariable Long videoId , HttpServletRequest httpServletRequest) {
              return streamingService.play(userId,videoId,httpServletRequest);
    }

    @GetMapping("/users/{userId}/videos/{videoId}/pause")
    public void pause(@PathVariable Long userId,
                      @PathVariable Long videoId,
                      @RequestParam int currentPosition,
                      @RequestParam  HttpServletRequest httpServletRequest ) {

            streamingService.pause(userId,videoId,currentPosition);

    }

    @PostMapping("/adviewcount")
    public void adviewcount(@RequestBody AdviewcountRequestDto adviewcountRequestDto){
            streamingService.adviewcount(adviewcountRequestDto);
    }

}
