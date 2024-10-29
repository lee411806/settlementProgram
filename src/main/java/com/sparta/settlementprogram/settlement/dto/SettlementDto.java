package com.sparta.settlementprogram.settlement.dto;

import com.sparta.settlementprogram.user.entity.User;
import com.sparta.settlementprogram.streaming.entity.Videos;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SettlementDto {
    private User user;
    private Videos video;
    private LocalDate playedDate;
    private String date;
    private Integer currentPosition;
}
