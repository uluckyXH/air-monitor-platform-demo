package com.uluckyxh.airmonitorplatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 图表数据查询参数VO
 * @author uluckyXH
 * @date 2025-02-13
 */
@Data
public class ChartQueryVO {
    /**
     * 设备MN号
     */
    @NotBlank(message = "设备MN号不能为空")
    private String mn;

    /**
     * 开始时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
