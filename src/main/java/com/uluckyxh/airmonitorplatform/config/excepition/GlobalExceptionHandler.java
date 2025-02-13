package com.uluckyxh.airmonitorplatform.config.excepition;

import com.uluckyxh.airmonitorplatform.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.BindException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(value = AirQualityMonitoringException.class)
    public Result<String> handleAirQualityMonitoringException(AirQualityMonitoringException e) {
//        log.error("业务异常：{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMsg());
    }

    /**
     * 处理参数校验异常（@Valid注解抛出）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        String errorMessage = fieldErrors.stream()
                .map(error -> {
                    String field = error.getField();
                    String defaultMessage = error.getDefaultMessage();
                    // 处理日期格式错误
                    if (defaultMessage != null && defaultMessage.contains("LocalDateTime")) {
                        return field + ": 日期格式错误，正确格式为 yyyy-MM-dd HH:mm:ss";
                    }
                    return field + ": " + defaultMessage;
                })
                .collect(Collectors.joining("; "));

        log.error("参数校验失败: {}", errorMessage);
        return Result.error(errorMessage);
    }


}