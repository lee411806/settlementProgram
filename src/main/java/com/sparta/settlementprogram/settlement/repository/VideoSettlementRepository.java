package com.sparta.settlementprogram.settlement.repository;

import com.sparta.settlementprogram.settlement.entity.VideoSettlement;
import com.sparta.settlementprogram.streaming.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VideoSettlementRepository extends JpaRepository<VideoSettlement, Long> {
    @Query("SELECT v FROM VideoSettlement v WHERE v.video = :video AND v.startDate BETWEEN :startDate AND :endDate")
    List<VideoSettlement> findByVideoAndDateRange(@Param("video") Videos video,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

}
