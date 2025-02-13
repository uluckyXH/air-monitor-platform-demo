package com.uluckyxh.airmonitorplatform.job;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 自动建表组件
 * @author uluckyXH
 * @date 2025-02-13
 */
@Slf4j
@Component
public class TableInitializer {

    // JdbcTemplate 是 Spring 提供的 JDBC 操作模板类
    // 它简化了传统 JDBC 操作，无需手动处理连接、Statement、ResultSet 等资源
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 建表SQL模板
     * 使用占位符%s表示表名后缀（年月）
     */
    private static final String CREATE_TABLE_SQL = """
    CREATE TABLE IF NOT EXISTS air_quality_monitoring_%s (
        id VARCHAR(64) NOT NULL COMMENT '雪花算法生成的主键ID',
        mn VARCHAR(32) NOT NULL COMMENT '设备唯一编号(MN号)',
        monitor_time DATETIME NOT NULL COMMENT '监测时间',
        pm25 DECIMAL(10,2) COMMENT 'PM2.5细颗粒物浓度',
        pm10 DECIMAL(10,2) COMMENT 'PM10可吸入颗粒物浓度',
        co DECIMAL(10,3) COMMENT '一氧化碳浓度',
        no2 DECIMAL(10,2) COMMENT '二氧化氮浓度',
        so2 DECIMAL(10,2) COMMENT '二氧化硫浓度',
        o3 DECIMAL(10,2) COMMENT '臭氧浓度',
        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (id),
        UNIQUE KEY uk_mn_monitor_time (monitor_time, mn) COMMENT '设备号和监测时间的唯一索引',
        KEY idx_monitor_time (monitor_time) COMMENT '监测时间索引'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空气质量监测数据表';
    """;

    /**
     * 检查表是否存在的SQL
     * information_schema.TABLES 包含了数据库中所有表的信息
     */
    private static final String CHECK_TABLE_EXISTS_SQL = """
        SELECT COUNT(1) FROM information_schema.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = ?
        """;


    /**
     * 应用启动时自动执行初始化
     * 创建当前月和下个月的表
     */
    @PostConstruct
    public void init() {
        // 创建当前月份的表
        createTableIfNotExists(LocalDateTime.now());
        // 创建下个月的表
        createTableIfNotExists(LocalDateTime.now().plusMonths(1));
    }

    /**
     * 定时任务：每月25号凌晨1点执行
     * 创建下个月的表
     */
    @Scheduled(cron = "0 0 1 25 * ?")
    public void createNextMonthTable() {
        createTableIfNotExists(LocalDateTime.now().plusMonths(1));
    }


    /**
     * 创建指定月份的分表
     * @param dateTime 指定月份的时间
     */
    private void createTableIfNotExists(LocalDateTime dateTime) {
        // 生成表名后缀（yyyyMM格式）
        String tableSuffix = dateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 完整表名
        String tableName = "air_quality_monitoring_" + tableSuffix;

        try {
            // 首先检查表是否存在
            boolean tableExists = checkTableExists(tableName);

            if (tableExists) {
                log.info("Table {} 已经存在，无需重复创建", tableName);
                return;
            }

            // 生成建表SQL
            String sql = String.format(CREATE_TABLE_SQL, tableSuffix);

            // 执行建表SQL
            jdbcTemplate.execute(sql);
            log.info("创建表 {} 成功", tableName);

        } catch (DataAccessException e) {
            log.error("数据库操作失败: {}", tableName, e);
            throw new RuntimeException("Table operation failed", e);
        } catch (Exception e) {
            log.error("创建表执行SQL异常: {}", tableName, e);
            throw new RuntimeException("Table operation failed", e);
        }
    }

    /**
     * 检查表是否存在
     * @param tableName 完整的表名
     * @return true如果表存在，否则false
     */
    private boolean checkTableExists(String tableName) {
        try {
            // 查询表是否存在
            // queryForObject 用于执行查询并返回单个结果
            // 第一个参数是SQL语句
            // 第二个参数是SQL参数数组
            // 第三个参数是返回类型
            Integer count = jdbcTemplate.queryForObject(
                    CHECK_TABLE_EXISTS_SQL,
                    Integer.class,
                    tableName
            );
            return count != null && count > 0;

        } catch (DataAccessException e) {
            log.error("查询表是否存在异常 {}", tableName, e);
            throw new RuntimeException("查询表是否存在异常", e);
        }
    }


}
