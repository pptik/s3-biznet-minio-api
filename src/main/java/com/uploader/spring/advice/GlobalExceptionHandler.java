package com.uploader.spring.advice;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.uploader.spring.models.dto.ResponseMessageWrapper;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseMessageWrapper> handleIOException(IOException ex, WebRequest request) {
        log.error("IO Exception occured", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ResponseMessageWrapper(
                                HttpStatus.BAD_REQUEST.value(),
                                Boolean.FALSE,
                                ex.getMessage()));
    }
}
