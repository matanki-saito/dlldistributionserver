package com.popush.triela.common.exception;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
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
        errorMap.put("status", HttpStatus.METHOD_NOT_ALLOWED);

        log.warn("HttpRequestMethodNotSupportedException={}", e);

        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler({BindException.class, ArgumentException.class})
    @ResponseBody
    public Map<String, Object> handleErrorBindException(Exception e) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("title", "Bad request");
        errorMap.put("status", HttpStatus.BAD_REQUEST);

        log.warn("BindException|ArgumentException={}", e);

        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @org.springframework.web.bind.annotation.ExceptionHandler({OtherSystemException.class})
    @ResponseBody
    public Map<String, Object> handleErrorOtherServiceException(OtherSystemException e) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("title", "Other system is down");
        errorMap.put("status", HttpStatus.BAD_GATEWAY);

        log.warn("OtherSystemException={}", e);

        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler({RequestRejectedException.class})
    @ResponseBody
    public Map<String, Object> handleErrorRequestRejectedException(RequestRejectedException e) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("title", "Reject request");
        errorMap.put("status", HttpStatus.BAD_REQUEST);

        log.warn("RequestRejectedException={}", e);

        return errorMap;
    }
}
