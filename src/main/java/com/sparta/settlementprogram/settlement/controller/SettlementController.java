package com.sparta.settlementprogram.settlement.controller;

import com.sparta.settlementprogram.settlement.dto.SettlementDto;
import com.sparta.settlementprogram.settlement.service.SettlementService;
import com.sparta.settlementprogram.streaming.entity.Videos;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;


    @GetMapping("/statistics")
    public void statistics(@RequestParam String date) {
        settlementService.statistics(date);
    }


    @PostMapping("/settlement")
    public void settlement(@RequestBody SettlementDto settlementDto) {
            settlementService.settlement(settlementDto);
    }

    @GetMapping("/view/settlement")
    public void viewSettlement(@RequestParam Videos video , @RequestParam String date) {
        settlementService.viewSettlement(video,date);
    }


}
