package com.uluckyxh.airmonitorplatform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 月份查询信息
 */
@Data
@AllArgsConstructor
public class MonthQueryInfo {
    /**
     * 表名
     */
    private String tableName;

    /**
     * 该月份查询的开始时间
     */
    private LocalDateTime startTime;

    /**
     * 该月份查询的结束时间
     */
    private LocalDateTime endTime;
}