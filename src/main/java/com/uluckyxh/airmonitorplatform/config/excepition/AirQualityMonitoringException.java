package com.uluckyxh.airmonitorplatform.config.excepition;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirQualityMonitoringException extends RuntimeException {

    private Integer code;

    private String msg;

    public AirQualityMonitoringException(String msg, Object... arguments) {
        super(StrUtil.format(msg, arguments));
        this.code = 500;
        this.msg = StrUtil.format(msg, arguments);
    }

    public AirQualityMonitoringException(Integer code, String msg, Object... arguments) {
        super(StrUtil.format(msg, arguments));
        this.code = code;
        this.msg = StrUtil.format(msg, arguments);
    }

}
