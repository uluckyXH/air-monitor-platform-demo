package com.uluckyxh.airmonitorplatform.controller;

import com.uluckyxh.airmonitorplatform.common.Result;
import com.uluckyxh.airmonitorplatform.entity.AirQualityMonitoring;
import com.uluckyxh.airmonitorplatform.service.AirQualityMonitoringService;
import com.uluckyxh.airmonitorplatform.vo.AirQualityMonitoringBriefVo;
import com.uluckyxh.airmonitorplatform.vo.ChartQueryVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/air-quality")
@Slf4j
public class AirQualityMonitoringController {


    @Autowired
    private AirQualityMonitoringService airQualityMonitoringService;

    /**
     * 查询设备监测数据用于图表展示
     */
    @GetMapping("/data")
    public Result<List<AirQualityMonitoringBriefVo>> queryChartData(@Valid ChartQueryVO query) {
        try {
            log.info("查询图表数据 - 设备: {}, 时间范围: {} 到 {}",
                    query.getMn(), query.getStartTime(), query.getEndTime());

            List<AirQualityMonitoringBriefVo> data = airQualityMonitoringService.queryForChart(
                    query.getMn(), query.getStartTime(), query.getEndTime());

            return Result.success(data);

        } catch (DateTimeParseException e) {
            log.error("日期格式错误", e);
            return Result.error("日期格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");

        } catch (Exception e) {
            log.error("查询图表数据失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 单记录插入接口
     */

    @PostMapping("/save")
    public Result<AirQualityMonitoring> save(@RequestBody @Valid AirQualityMonitoring
                                                     airQualityMonitoring) {
        boolean save = airQualityMonitoringService.saveAirQualityMonitoring(airQualityMonitoring);
        if (!save) {
            return Result.error("保存失败");
        }
        return Result.success(airQualityMonitoring);
    }

    /**
     * 批量插入
     */
    @PostMapping("/saveBatch")
    public Result<List<AirQualityMonitoring>> saveBatch(@RequestBody @Valid List<AirQualityMonitoring>
                                                                    airQualityMonitoringList) {
        boolean save = airQualityMonitoringService.saveBatchAirQualityMonitoring(airQualityMonitoringList);
        if (!save) {
            return Result.error("保存失败");
        }
        return Result.success();
    }

}
