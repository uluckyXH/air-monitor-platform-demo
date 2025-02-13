package com.uluckyxh.airmonitorplatform.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdGeneratorUtil {

    /**
     * 雪花算法单例对象
     * workerId和datacenterId都设置为1
     */
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 生成带连字符的UUID
     * 例如：a5c8a5e8-df2b-4706-bea4-08d0939410e3
     *
     * @return 36位UUID字符串
     */
    public static String randomUUID() {
        String uuid = IdUtil.randomUUID();
        log.debug("Generated UUID with hyphen: {}", uuid);
        return uuid;
    }

    /**
     * 生成不带连字符的UUID
     * 例如：b17f24ff026d40949c85a24f4f375d42
     *
     * @return 32位UUID字符串
     */
    public static String simpleUUID() {
        String uuid = IdUtil.simpleUUID();
        log.debug("Generated UUID without hyphen: {}", uuid);
        return uuid;
    }

    /**
     * 生成MongoDB风格的ObjectId
     * 例如：5b9e306a4df4f8c54a39fb0c
     *
     * @return 24位ObjectId字符串
     */
    public static String objectId() {
        String objectId = IdUtil.objectId();
        log.debug("Generated ObjectId: {}", objectId);
        return objectId;
    }

    /**
     * 生成雪花算法ID（Long类型）
     *
     * @return Long类型雪花算法ID
     */
    public static long snowflakeId() {
        long id = SNOWFLAKE.nextId();
        log.debug("Generated Snowflake ID: {}", id);
        return id;
    }

    /**
     * 生成雪花算法ID（字符串类型）
     *
     * @return 字符串类型雪花算法ID
     */
    public static String snowflakeIdStr() {
        String id = SNOWFLAKE.nextIdStr();
        log.debug("Generated Snowflake ID String: {}", id);
        return id;
    }

    /**
     * 使用简单方式生成雪花算法ID（Long类型）
     * 注意：此方法每次调用会创建新的Snowflake对象，不推荐频繁使用
     *
     * @return Long类型雪花算法ID
     */
    public static long getSnowflakeNextId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 使用简单方式生成雪花算法ID（字符串类型）
     * 注意：此方法每次调用会创建新的Snowflake对象，不推荐频繁使用
     *
     * @return 字符串类型雪花算法ID
     */
    public static String getSnowflakeNextIdStr() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    // 防止实例化
    private IdGeneratorUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}