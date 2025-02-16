package com.uluckyxh.airmonitorplatform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导出任务分组类，包含分页信息
 */
@Data
@AllArgsConstructor
public class ExportTaskGroup {
    // 表名
    private String tableName;
    // 监测点编号
    private String mn;
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;
    // Excel中的起始行号
    private int startRow;
    // Excel中的结束行号
    private int endRow;
    // 当前页码
    private int pageNum;
    // 每页数据量
    private int pageSize;
    // 本批次实际数据量
    private int actualSize;
}
