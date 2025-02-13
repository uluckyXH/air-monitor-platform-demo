package com.uluckyxh.airmonitorplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uluckyxh.airmonitorplatform.config.excepition.AirQualityMonitoringException;
import com.uluckyxh.airmonitorplatform.utils.RequestDataHelper;
import com.uluckyxh.airmonitorplatform.entity.AirQualityMonitoring;
import com.uluckyxh.airmonitorplatform.mapper.AirQualityMonitoringMapper;
import com.uluckyxh.airmonitorplatform.service.AirQualityMonitoringService;
import com.uluckyxh.airmonitorplatform.vo.AirQualityMonitoringBriefVo;
import com.uluckyxh.airmonitorplatform.vo.MonthQueryInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AirQualityMonitoringServiceImpl extends ServiceImpl<AirQualityMonitoringMapper, AirQualityMonitoring>
        implements AirQualityMonitoringService {


    @Autowired
    private AirQualityMonitoringMapper airQualityMonitoringMapper;

    /**
     * 表名前缀
     */
    private static final String TABLE_PREFIX = "air_quality_monitoring_";


    /**
     * 按时间范围查询设备监测数据
     *
     * @param mn        设备号
     * @param startTime 开始时间 例如：2025-02-12 15:30:00
     * @param endTime   结束时间 例如：2025-03-13 05:27:05
     * @return 监测数据列表
     */
    public List<AirQualityMonitoringBriefVo> queryForChart(String mn, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 获取查询涉及的所有月份及其对应的时间范围
        List<MonthQueryInfo> monthQueries = calculateMonthlyQueries(startTime, endTime);

        // 2. 按月查询并合并结果
        List<AirQualityMonitoringBriefVo> result = new ArrayList<>();
        for (MonthQueryInfo monthQuery : monthQueries) {
            // 查询单个月份的数据
            List<AirQualityMonitoringBriefVo> monthData = airQualityMonitoringMapper.selectByTableAndTimeRange(
                    monthQuery.getTableName(),
                    mn,
                    monthQuery.getStartTime(),
                    monthQuery.getEndTime()
            );
            result.addAll(monthData);
        }

        return result;
    }

    @Override
    public boolean saveAirQualityMonitoring(AirQualityMonitoring airQualityMonitoring) {
        try {
            // 设置分表参数
            RequestDataHelper.setRequestData(new HashMap<String, Object>() {{
                put("monitor_time", airQualityMonitoring.getMonitorTime());
            }});

            // 2. 直接保存，让数据库的唯一索引来处理重复数据
            return save(airQualityMonitoring);
        } catch (DuplicateKeyException e) {
            log.warn("数据重复 - 设备: {}, 时间: {}",
                    airQualityMonitoring.getMn(),
                    airQualityMonitoring.getMonitorTime());
            throw new AirQualityMonitoringException("该时间点的数据已存在");
        } finally {
            RequestDataHelper.remove();
        }
    }

    /**
     * 重写批量插入方法，处理分表逻辑
     */
    @Override
    public boolean saveBatchAirQualityMonitoring(List<AirQualityMonitoring> entityList) {
        try {
            // 按月份分组
            Map<String, List<AirQualityMonitoring>> groupByMonth = entityList.stream()
                    .collect(Collectors.groupingBy(entity ->
                            entity.getMonitorTime().format(DateTimeFormatter.ofPattern("yyyyMM"))));

            // 分组插入
            for (Map.Entry<String, List<AirQualityMonitoring>> entry : groupByMonth.entrySet()) {
                // 获取该组任意一条数据的时间作为分表依据
                LocalDateTime monthTime = entry.getValue().get(0).getMonitorTime();

                // 设置分表参数
                RequestDataHelper.setRequestData(new HashMap<String, Object>() {{
                    put("monitor_time", monthTime);
                }});

                // 执行批量插入
                super.saveBatch(entry.getValue());
            }
            return true;
        } finally {
            RequestDataHelper.remove();
        }
    }

    private AirQualityMonitoring getExistingRecord(String mn, LocalDateTime monitorTime) {
        // 不能为空
        if (StrUtil.isBlank(mn)) {
            throw new AirQualityMonitoringException("设备号不能为空");
        }

        // 判断时间是否为空
        if (null == monitorTime) {
            throw new AirQualityMonitoringException("监测时间不能为空");
        }

        // 拿到年月，用于确定表名
        YearMonth yearMonth = YearMonth.from(monitorTime);
        return airQualityMonitoringMapper.selectExistingRecord(
                TABLE_PREFIX + yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")),
                mn,
                monitorTime
        );
    }

    /**
     * 计算每个月份的查询时间范围
     * 例如：2025-02-12 15:30:00 到 2025-03-13 05:27:05
     * 会被拆分为：
     * 2025-02: 2025-02-12 15:30:00 到 2025-02-28 23:59:59
     * 2025-03: 2025-03-01 00:00:00 到 2025-03-13 05:27:05
     */
    private List<MonthQueryInfo> calculateMonthlyQueries(LocalDateTime startTime, LocalDateTime endTime) {
        // 创建一个列表存储每个月的查询信息
        List<MonthQueryInfo> queries = new ArrayList<>();

        // 从开始时间和结束时间中提取年月信息
        // 如：2025-02-12 15:30:00 -> 2025-02
        YearMonth startMonth = YearMonth.from(startTime);

        // 如：2025-03-13 05:27:05 -> 2025-03
        YearMonth endMonth = YearMonth.from(endTime);

        // 初始化当前处理的年月为开始月份
        YearMonth currentMonth = startMonth;

        // 循环处理每个月份，直到处理完结束月份
        while (!currentMonth.isAfter(endMonth)) {
            // 声明当前月份的开始和结束时间变量
            LocalDateTime monthStartTime;
            LocalDateTime monthEndTime;

            // 确定当前月份的开始时间
            if (currentMonth.equals(startMonth)) {
                // 如果是第一个月，使用传入的开始时间
                // 如：2025-02-12 15:30:00
                monthStartTime = startTime;
            } else {
                // 如果不是第一个月，使用当月1号的0点
                // 如：2025-03-01 00:00:00
                monthStartTime = currentMonth.atDay(1).atStartOfDay();
            }

            // 确定当前月份的结束时间
            if (currentMonth.equals(endMonth)) {
                // 如果是最后一个月，使用传入的结束时间
                // 如：2025-03-13 05:27:05
                monthEndTime = endTime;
            } else {
                // 如果不是最后一个月，使用当月最后一天的23:59:59
                // 如：2025-02-28 23:59:59
                monthEndTime = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            }

            // 构建表名，如：air_quality_monitoring_202502
            String tableName = TABLE_PREFIX + currentMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

            // 创建并添加当前月份的查询信息
            queries.add(new MonthQueryInfo(tableName, monthStartTime, monthEndTime));

            // 移动到下一个月
            currentMonth = currentMonth.plusMonths(1);
        }

        // 返回所有月份的查询信息
        return queries;
    }
}
