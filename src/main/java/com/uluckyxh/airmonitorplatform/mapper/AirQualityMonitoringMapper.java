package com.uluckyxh.airmonitorplatform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uluckyxh.airmonitorplatform.entity.AirQualityMonitoring;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface AirQualityMonitoringMapper extends BaseMapper<AirQualityMonitoring> {
    /**
     * 查询指定月份表的数据
     */
    @Select("SELECT * FROM ${tableName} " +
            "WHERE mn = #{mn} " +
            "AND monitor_time >= #{startTime} " +
            "AND monitor_time <= #{endTime} " +
            "ORDER BY monitor_time ASC")
    List<AirQualityMonitoring> selectByTableAndTimeRange(
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

}
