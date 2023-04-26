package com.netflix.conductor.rest.controllers;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.netflix.conductor.common.validation.ErrorResponse;
import com.netflix.conductor.core.exception.ApplicationException;
import com.netflix.conductor.core.exception.ConflictException;
import com.netflix.conductor.core.exception.NotFoundException;
import com.netflix.conductor.core.utils.Utils;
import com.netflix.conductor.metrics.Monitors;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
@Order(ValidationExceptionMapper.ORDER + 1)
public class ApplicationExceptionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionMapper.class);

    private final String host = Utils.getServerId();

    private static final Map<Class<? extends Throwable>, HttpStatus> EXCEPTION_STATUS_MAP = new HashMap<>();

    static {
        EXCEPTION_STATUS_MAP.put(NotFoundException.class, HttpStatus.NOT_FOUND);
        EXCEPTION_STATUS_MAP.put(ConflictException.class, HttpStatus.CONFLICT);
        EXCEPTION_STATUS_MAP.put(IllegalArgumentException.class, HttpStatus.BAD_REQUEST);
        EXCEPTION_STATUS_MAP.put(InvalidFormatException.class, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(HttpServletRequest request, ApplicationException ex) {
        logException(request, ex);
        Monitors.error("error", String.valueOf(ex.getHttpStatusCode()));
        return new ResponseEntity<>(toErrorResponse(ex), HttpStatus.valueOf(ex.getHttpStatusCode()));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleAll(HttpServletRequest request, Throwable th) {
        logException(request, th);
        HttpStatus status = EXCEPTION_STATUS_MAP.getOrDefault(th.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setInstance(host);
        errorResponse.setStatus(status.value());
        errorResponse.setMessage(th.getMessage());
        errorResponse.setRetryable(false);
        Monitors.error("error", String.valueOf(status.value()));
        return new ResponseEntity<>(errorResponse, status);
    }

    private void logException(HttpServletRequest request, Throwable exception) {
        LOGGER.error("Error {} url: '{}'", exception.getClass().getSimpleName(), request.getRequestURI(), exception);
    }

    private ErrorResponse toErrorResponse(ApplicationException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setInstance(host);
        errorResponse.setStatus(ex.getHttpStatusCode());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setRetryable(ex.isRetryable());
        return errorResponse;
    }
}
