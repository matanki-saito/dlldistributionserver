package com.popush.triela.common.exception;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    public Map<String, Object> handleErrorHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("message", e.getMessage());
        errorMap.put("status", HttpStatus.METHOD_NOT_ALLOWED);
        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler({BindException.class, ArgumentException.class})
    @ResponseBody
    public Map<String, Object> handleErrorBindException(Exception e) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("title", "Bad request");
        errorMap.put("status", HttpStatus.BAD_REQUEST);
        errorMap.put("message", e.getMessage());

        log.error("BindException|ArgumentException={}", e);

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
