package com.uluckyxh.airmonitorplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uluckyxh.airmonitorplatform.config.excepition.AirQualityMonitoringException;
import com.uluckyxh.airmonitorplatform.utils.RequestDataHelper;
import com.uluckyxh.airmonitorplatform.entity.AirQualityMonitoring;
import com.uluckyxh.airmonitorplatform.mapper.AirQualityMonitoringMapper;
import com.uluckyxh.airmonitorplatform.service.AirQualityMonitoringService;
import com.uluckyxh.airmonitorplatform.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AirQualityMonitoringServiceImpl extends ServiceImpl<AirQualityMonitoringMapper, AirQualityMonitoring> implements AirQualityMonitoringService {


    @Autowired
    private AirQualityMonitoringMapper airQualityMonitoringMapper;

    @Autowired
    @Qualifier("MyAsyncExecutor")
    private ThreadPoolTaskExecutor myAsyncExecutor;

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
            List<AirQualityMonitoringBriefVo> monthData = airQualityMonitoringMapper.selectByTableAndTimeRange(monthQuery.getTableName(), mn, monthQuery.getStartTime(), monthQuery.getEndTime());
            result.addAll(monthData);
            //
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
            log.warn("数据重复 - 设备: {}, 时间: {}", airQualityMonitoring.getMn(), airQualityMonitoring.getMonitorTime());
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
            Map<String, List<AirQualityMonitoring>> groupByMonth = entityList.stream().collect(Collectors.groupingBy(entity -> entity.getMonitorTime().format(DateTimeFormatter.ofPattern("yyyyMM"))));

            // 分组插入
            for (Map.Entry<String, List<AirQualityMonitoring>> entry : groupByMonth.entrySet()) {
                // 获取该组任意一条数据的时间作为分表依据
                LocalDateTime monthTime = entry.getValue().get(0).getMonitorTime();

                // 设置分表参数
                RequestDataHelper.setRequestData(new HashMap<>() {{
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

    /**
     * 导出Excel
     *
     * @param chartQueryVO 查询条件
     * @return Excel文件
     */
    @Override
    public XSSFWorkbook exportExcel(ChartQueryVO chartQueryVO) {
        // XSSFWorkbook 一般用于导出小于10000行的数据，全部数据加载到内存，文件格式.xlsx
        // SXSSFWorkbook 一般用于导出大于10000行的数据，流形式写入，节省内存，需要调用dispose方法释放资源，文件格式.xls
        // HSSFWorkbook 旧版的excel文件，文件格式.xls,最大支持65536行数据

        // 【注意】该方案只适用于10w+数据以下，因为用的是XSSFWorkbook，如果数据量大，需要使用SXSSFWorkbook

        //TODO 放一个另外的严谨问题，如果导出的不是历史数据，而是实时数据，要怎样保证多线程下的事务呢？
        //TODO //  @Transactional(isolation = Isolation.SERIALIZABLE) （bushi
        // 在SERIALIZABLE级别下，MySQL会：
        // 假设SQL是：SELECT * FROM users WHERE name LIKE 'Tom%'
        // 1. 对符合条件的记录加共享锁（S锁）
        // 2. 对范围条件加间隙锁（Gap Lock）
        // 3. 这些锁会阻止其他事务对这个范围的数据进行修改


        // 记录方法运行的时间：
        long start = System.currentTimeMillis();

        // 设置每页处理的数据量
        final int PAGE_SIZE = 1000;

        try {
            // 创建一个SXSSFWorkbook对象
            XSSFWorkbook workbook = new XSSFWorkbook();
            // 创建工作表
            Sheet sheet = workbook.createSheet("空气质量数据");
            // 创建表头
            createHeader(sheet);

            // 首先拿到所有的日期，计算出总共有多少条数据，分表在哪几张表
            List<MonthQueryInfo> monthQueries = calculateMonthlyQueries(chartQueryVO.getStartTime(), chartQueryVO.getEndTime());

            // 整一个Map集合来作为存储，对应的表+数据条数
            Map<String, ExportInfo> tableDataCount = new HashMap<>();

            // 循环遍历每个月份的查询数据总数
            monthQueries.forEach(monthQuery -> {
                String tableName = monthQuery.getTableName();
                String mn = chartQueryVO.getMn();
                LocalDateTime startTime = monthQuery.getStartTime();
                LocalDateTime endTime = monthQuery.getEndTime();

                // 查询单个月份的数据总数
                int count = airQualityMonitoringMapper.selectCountByTableAndTimeRange(tableName, mn, startTime, endTime);

                // 存储到Map中
                tableDataCount.put(monthQuery.getTableName(), new ExportInfo(mn, startTime, endTime, count));
            });

            // 创建任务分组列表
            List<ExportTaskGroup> taskGroups = new ArrayList<>();
            int currentRowNum = 1; // 从1开始、0是表头

            // 遍历每个表的数据统计信息，创建分页任务
            for (Map.Entry<String, ExportInfo> entry : tableDataCount.entrySet()) {
                // 表名
                String tableName = entry.getKey();
                // info信息
                ExportInfo info = entry.getValue();
                // 数据总数
                Integer dataCount = info.getDataCount();

                // 计算总页数
                int totalPages = (int) Math.ceil((double) dataCount / PAGE_SIZE);

                log.info("表：{} 总数据量：{} 分 {} 页处理", tableName, dataCount, totalPages);

                // 为每页创建一个任务组
                for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                    // 计算当前页的实际数据量
                    int actualSize = (pageNum == totalPages) ? dataCount - (pageNum - 1) * PAGE_SIZE : PAGE_SIZE;

                    // 创建任务分组
                    taskGroups.add(new ExportTaskGroup(tableName, info.getMn(), info.getStartTime(), info.getEndTime(), currentRowNum, // 起始行
                            currentRowNum + actualSize - 1, // 结束行
                            pageNum, // 页码
                            PAGE_SIZE, // 每页大小
                            actualSize  // 实际数据量
                    ));

                    currentRowNum += actualSize;
                }
            }

            // 添加同步机制
            Object writeLock = new Object();

            // 打印分组信息
//            taskGroups.forEach(group -> log.info("任务组: 表={}, 页码={}, 行范围={}-{}, 预期数据量={}", group.getTableName(), group.getPageNum(), group.getStartRow(), group.getEndRow(), group.getActualSize()));

            // 使用CompletableFuture并发处理每个组
            List<CompletableFuture<Void>> futures = taskGroups.stream().map(taskGroup -> CompletableFuture.runAsync(() -> {

                try {
                    // 使用分页参数查询数据
                    List<AirQualityMonitoringBriefVo> pageData = airQualityMonitoringMapper
                            .selectByTableAndTimeRangeWithPage(taskGroup.getTableName(), taskGroup.getMn(), taskGroup.getStartTime(), taskGroup.getEndTime(), (taskGroup.getPageNum() - 1) * taskGroup.getPageSize(), // 计算offset
                                    taskGroup.getPageSize());

                    synchronized (writeLock) {
                        // 写入数据到对应的行
                        int rowNum = taskGroup.getStartRow();
                        for (AirQualityMonitoringBriefVo data : pageData) {
                            Row row = sheet.createRow(rowNum++);
                            writeDataToRow(row, data);
                        }
                    }

                    log.info("完成任务组: 表={}, 页码={}, 实际处理数据量={}", taskGroup.getTableName(), taskGroup.getPageNum(), pageData.size());


                } catch (Exception e) {
                    log.error("导出Excel失败", e);
                    throw new AirQualityMonitoringException("导出失败：" + e.getMessage());
                }

            }, myAsyncExecutor)).toList();

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 方法结束时间
            long end = System.currentTimeMillis();
            // 转成秒
            log.info("导出Excel耗时：{}s", (end - start) / 1000);

            return workbook;
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new AirQualityMonitoringException("导出失败：" + e.getMessage());
        }

    }

    /**
     * 写入数据到行
     *
     * @param row  行对象
     * @param data 数据对象
     */
    private void writeDataToRow(Row row, AirQualityMonitoringBriefVo data) {
        row.createCell(0).setCellValue(data.getMn());
        row.createCell(1).setCellValue(data.getMonitorTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        row.createCell(2).setCellValue(data.getPm25().toString());
        row.createCell(3).setCellValue(data.getPm10().toString());
        row.createCell(4).setCellValue(data.getCo().toString());
        row.createCell(5).setCellValue(data.getNo2().toString());
        row.createCell(6).setCellValue(data.getSo2().toString());
        row.createCell(7).setCellValue(data.getO3().toString());
    }

    /**
     * 创建Excel表头
     *
     * @param sheet 工作表
     */
    private void createHeader(Sheet sheet) {
        // 创建第一行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"监测点", "监测时间", "PM2.5", "PM10", "CO", "NO2", "SO2", "O3"};


        // 创建表头样式
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();

        // 设置对齐方式，居中对齐
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // 设置字体号
        Font font = sheet.getWorkbook().createFont();
        font.setFontHeightInPoints((short) 20);
        // 加粗
        font.setBold(true);
        headerStyle.setFont(font);

        // 设置边框
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 设置行高
        headerRow.setHeightInPoints(30);

        // 创建单元格
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            // 设置值
            cell.setCellValue(headers[i]);
            // 设置样式
            cell.setCellStyle(headerStyle);
            // 设置每个列宽
            sheet.setColumnWidth(i, 20 * 256);
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
        return airQualityMonitoringMapper.selectExistingRecord(TABLE_PREFIX + yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")), mn, monitorTime);
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
