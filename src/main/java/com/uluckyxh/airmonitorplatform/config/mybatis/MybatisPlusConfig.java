package com.uluckyxh.airmonitorplatform.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.uluckyxh.airmonitorplatform.utils.RequestDataHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Configuration
@EnableTransactionManagement  // 启用事务管理
@MapperScan("com.uluckyxh.airmonitorplatform.mapper")  // 扫描Mapper接口包路径
public class MybatisPlusConfig {

    // 需要分表的表名常量
    private static final String SHARDING_TABLE_NAME = "air_quality_monitoring";

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 创建动态表名插件实例
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();

        // 设置动态表名处理器
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            // 只处理空气质量监测表
            if (!SHARDING_TABLE_NAME.equals(tableName)) {
                return tableName;
            }

            // 获取参数方法
            Map<String, Object> paramMap = RequestDataHelper.getRequestData();
            if (paramMap != null) {
                // 打印参数便于调试
//                paramMap.forEach((k, v) -> System.err.println(k + "----" + v));

                // 获取监测时间参数
                LocalDateTime monitorTime = (LocalDateTime) paramMap.get("monitor_time");
                if (monitorTime != null) {
                    // 返回带月份后缀的表名
                    return tableName + "_" + monitorTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
                }
            }

            return tableName;
        });

        // 将动态表名插件添加到插件链中
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);

        // 配置分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L); // 设置最大分页限制
        paginationInterceptor.setOverflow(false); // 设置超出最大分页限制时是否返回全部数据
        interceptor.addInnerInterceptor(paginationInterceptor); // 添加分页插件

        // 配置防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

}
