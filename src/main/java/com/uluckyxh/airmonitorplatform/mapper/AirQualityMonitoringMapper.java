package com.uluckyxh.airmonitorplatform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uluckyxh.airmonitorplatform.entity.AirQualityMonitoring;
import com.uluckyxh.airmonitorplatform.vo.AirQualityMonitoringBriefVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface AirQualityMonitoringMapper extends BaseMapper<AirQualityMonitoring> {
    /**
     * 查询指定月份表的数据
     */
    @Select("SELECT mn,monitor_time,pm25,pm10,co,no2,so2,o3 FROM ${tableName} " +
            "WHERE monitor_time >= #{startTime} " +
            "AND monitor_time <= #{endTime} " +
            "AND mn = #{mn}  " +
            "ORDER BY monitor_time ASC")
    List<AirQualityMonitoringBriefVo> selectByTableAndTimeRange(
            @Param("tableName") String tableName,
            @Param("mn") String mn,
                @Param("startTime") LocalDateTime startTime,
                @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM  ${tableName} " +
            "WHERE mn = #{mn} " +
            "AND monitor_time = #{monitorTime}")
    AirQualityMonitoring selectExistingRecord(@Param("tableName") String tableName,
                                              @Param("mn") String mn,
                                              @Param("monitorTime") LocalDateTime monitorTime);

    @Select("SELECT COUNT(id) FROM ${tableName} " +
            "WHERE monitor_time >= #{startTime} " +
            "AND monitor_time <= #{endTime} " +
            "AND mn = #{mn}")
    int selectCountByTableAndTimeRange(
            @Param("tableName") String tableName,
            @Param("mn") String mn,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("SELECT mn,monitor_time,pm25,pm10,co,no2,so2,o3 FROM ${tableName} " +
            "WHERE monitor_time >= #{startTime} " +
            "AND monitor_time <= #{endTime} " +
            "AND mn = #{mn}  " +
            "ORDER BY monitor_time ASC " +
            "LIMIT #{startRow},#{pageSize}")
    List<AirQualityMonitoringBriefVo> selectByTableAndTimeRangeWithPage(
            @Param("tableName") String tableName,
            @Param("mn") String mn,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("startRow") int startRow,
            @Param("pageSize") int pageSize);
}
