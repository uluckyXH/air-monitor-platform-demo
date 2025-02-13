package com.uluckyxh.airmonitorplatform.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

        // 配置动态表名拦截器（分表功能）
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        // 设置表名处理器，只对 air_quality_monitoring 表进行分表处理
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            // 只有空气质量监测表需要分表
            if (SHARDING_TABLE_NAME.equals(tableName)) {
                // 获取当前时间并格式化为yyyyMM格式作为表名后缀
                String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
                // 返回分表后的表名
                return tableName + "_" + suffix;
            }
            // 其他表返回原表名
            return tableName;
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);

        // 配置分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L);
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 配置防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

}
