package com.uluckyxh.airmonitorplatform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ExportInfo {

    /**
     * mn
     */
    private String mn;

    /**
     * 该月份查询的开始时间
     */
    private LocalDateTime startTime;

    /**
     * 该月份查询的结束时间
     */
    private LocalDateTime endTime;

    /**
     * 数据条数
     */
    private Integer dataCount;

}
