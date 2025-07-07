package com.uploader.spring.controller;

import org.springframework.web.bind.annotation.RestController;

import com.uploader.spring.models.dto.ResponseWrapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class DefaultController {

    @GetMapping("/")
    public ResponseEntity<ResponseWrapper<String>> defaultEndpoint() {
        String dataWelcome = "Welcome to springboot uploader";

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseWrapper<>(
                        HttpStatus.OK.value(),
                        true,
                        "Success",
                        dataWelcome));
    }

}
