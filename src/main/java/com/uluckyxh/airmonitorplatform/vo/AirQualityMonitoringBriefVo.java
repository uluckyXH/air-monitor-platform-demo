package com.uluckyxh.airmonitorplatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AirQualityMonitoringBriefVo {


    /**
     * 设备唯一编号(MN号)
     */
    private String mn;

    /**
     * 监测时间,格式:yyyy-MM-dd HH:mm:ss
     */
    private LocalDateTime monitorTime;

    /**
     * PM2.5细颗粒物浓度,单位:μg/m³
     */
    private BigDecimal pm25;

    /**
     * PM10可吸入颗粒物浓度,单位:μg/m³
     */
    private BigDecimal pm10;

    /**
     * 一氧化碳浓度,单位:mg/m³
     */
    private BigDecimal co;

    /**
     * 二氧化氮浓度,单位:μg/m³
     */
    private BigDecimal no2;

    /**
     * 二氧化硫浓度,单位:μg/m³
     */
    private BigDecimal so2;

    /**
     * 臭氧浓度,单位:μg/m³
     */
    private BigDecimal o3;

}
