package com.ljt.study.exception;

import com.ljt.study.core.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author LiJingTang
 * @date 2024-06-04 14:54
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public R<?> exceptionHandler(Exception e) {
        log.error("发生异常了", e);
        return R.error(e.getMessage());
    }

}
