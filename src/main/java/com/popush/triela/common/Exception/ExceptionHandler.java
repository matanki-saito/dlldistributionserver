package com.popush.triela.common.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandler {
    @ResponseStatus(HttpStatus.GONE)
    @org.springframework.web.bind.annotation.ExceptionHandler({NotModifiedException.class})
    @ResponseBody
    public Map<String, Object> handleErrorNotModifiedException() {
        Map<String, Object> errorMap = new HashMap<>();
        return errorMap;
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
    @org.springframework.web.bind.annotation.ExceptionHandler({BindException.class})
    @ResponseBody
    public Map<String, Object> handleErrorBindException() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("message", "許可されていないメソッド");
        errorMap.put("status", HttpStatus.BAD_REQUEST);
        return errorMap;
    }
}
