package com.uluckyxh.airmonitorplatform.controller;

import com.uluckyxh.airmonitorplatform.common.Result;
import com.uluckyxh.airmonitorplatform.entity.AirQualityMonitoring;
import com.uluckyxh.airmonitorplatform.service.AirQualityMonitoringService;
import com.uluckyxh.airmonitorplatform.vo.AirQualityMonitoringBriefVo;
import com.uluckyxh.airmonitorplatform.vo.ChartQueryVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
    @PostMapping("/save/batch")
    public Result<List<AirQualityMonitoring>> saveBatch(@RequestBody @Valid List<AirQualityMonitoring>
                                                                airQualityMonitoringList) {
        boolean save = airQualityMonitoringService.saveBatchAirQualityMonitoring(airQualityMonitoringList);
        if (!save) {
            return Result.error("保存失败");
        }

        return Result.success();
    }

    /**
     * 导出Excel\
     */
    @GetMapping("/export")
    public void exportExcel(@Valid ChartQueryVO chartQueryVO, HttpServletResponse response) {

        XSSFWorkbook sxssfWorkbook = null;
        try {

            // 生成Excel
            sxssfWorkbook = airQualityMonitoringService.exportExcel(chartQueryVO);


            // 拿出日期来
            LocalDateTime startTime = chartQueryVO.getStartTime();
            LocalDateTime endTime = chartQueryVO.getEndTime();

            // 构造文件名
            String fileName = "设备" + chartQueryVO.getMn() + "的"
                    + startTime + "到"
                    + endTime + "的监测数据.xlsx";

            // 使用UTF-8编码文件名
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");    // 替换空格编码


            // 3. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");            // 使用RFC 5987标准处理文件名
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName);
            sxssfWorkbook.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("导出Excel失败", e);
        } finally {
            // 判断是否为空
            if (null != sxssfWorkbook) {
                try {
                    sxssfWorkbook.close();
                } catch (Exception e) {
                    log.error("关闭SXSSFWorkbook失败", e);
                }
            }

        }

    }

}
