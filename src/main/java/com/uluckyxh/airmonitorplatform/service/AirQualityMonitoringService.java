package com.uluckyxh.airmonitorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uluckyxh.airmonitorplatform.entity.AirQualityMonitoring;
import com.uluckyxh.airmonitorplatform.vo.AirQualityMonitoringBriefVo;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public interface AirQualityMonitoringService extends IService<AirQualityMonitoring> {

    List<AirQualityMonitoringBriefVo> queryForChart(
            String mn,
            LocalDateTime startTime,
            LocalDateTime endTime);

    boolean saveAirQualityMonitoring(AirQualityMonitoring airQualityMonitoring);

    boolean saveBatchAirQualityMonitoring(List<AirQualityMonitoring> airQualityMonitoringList);

}
