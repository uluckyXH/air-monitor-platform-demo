package com.uluckyxh.airmonitorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 空气质量监测数据实体类
 *
 * @author uluckyXH
 * @since 2025-02-12
 */
@Data
@Accessors(chain = true)
@TableName("air_quality_monitoring")
public class AirQualityMonitoring {

    /**
     * 雪花算法生成的主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 设备唯一编号(MN号)
     */
    private String mn;

    /**
     * 监测时间,格式:yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    /**
     * 数据入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createTime;

    /**
     * 数据更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时自动填充
    private LocalDateTime updateTime;
}