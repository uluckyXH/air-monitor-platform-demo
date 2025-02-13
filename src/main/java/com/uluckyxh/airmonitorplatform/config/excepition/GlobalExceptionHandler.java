package com.uluckyxh.airmonitorplatform.config.excepition;

import com.uluckyxh.airmonitorplatform.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
// 全局异常处理器
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    // 处理其他所有异常
    public Result<String> handleException(Exception e) {
        log.error("未知异常：{}", e.getMessage(), e);
        return Result.error("哎呀，服务器繁忙啦！");
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(NoResourceFoundException e) {
//        log.error("文件不存在：{}", e.getMessage(), e);
        return Result.error("访问的内容不存在");
    }

}