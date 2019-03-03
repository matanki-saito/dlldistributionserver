package com.popush.triela.common.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ExceptionHandler {
    @ResponseStatus(HttpStatus.GONE)
    @org.springframework.web.bind.annotation.ExceptionHandler({NotModifiedException.class})
    @ResponseBody
    public Map<String, Object> handleErrorNotModifiedException() {
        return new HashMap<>();
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @org.springframework.web.bind.annotation.ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    public Map<String, Object> handleErrorHttpRequestMethodNotSupportedException() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("message", "許可されていないメソッド");
        errorMap.put("status", HttpStatus.METHOD_NOT_ALLOWED);
        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler({BindException.class, ArgumentException.class})
    @ResponseBody
    public Map<String, Object> handleErrorBindException() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("message", "(´・ω・｀)");
        errorMap.put("status", HttpStatus.BAD_REQUEST);
        return errorMap;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler({RuntimeException.class})
    @ResponseBody
    public Map<String, Object> handleErrorIllegalStateOrIllegalArgumentException(RuntimeException e) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("title", "Runtime error");
        errorMap.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
        errorMap.put("message", e.getMessage());

        log.error("RuntimeException={}", e);

        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @org.springframework.web.bind.annotation.ExceptionHandler({OtherSystemException.class})
    @ResponseBody
    public Map<String, Object> handleErrorOtherServiceException(OtherSystemException e) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("title", "other system is down");
        errorMap.put("status", HttpStatus.BAD_GATEWAY);
        errorMap.put("message", e.getMessage());

        log.error("OtherSystemException={}", e);

        return errorMap;
    }

}
