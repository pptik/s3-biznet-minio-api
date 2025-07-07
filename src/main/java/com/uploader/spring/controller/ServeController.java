package com.uploader.spring.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uploader.spring.models.dto.MinioDonwloadFileDto;
import com.uploader.spring.service.ServeMinioService;
import com.uploader.spring.utils.constant.ApiPathConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(ApiPathConstant.API +
        ApiPathConstant.VERSION +
        "/serve")
public class ServeController {

    @Autowired
    private ServeMinioService serveMinioService;

    @GetMapping("/{filename}")
    public ResponseEntity<InputStreamResource> serveMinioFile(@PathVariable String filename) throws IOException {
        MinioDonwloadFileDto minioDonwloadFileDto = this.serveMinioService.serveFile(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, minioDonwloadFileDto.getContentType());
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(minioDonwloadFileDto.getContentLength()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(minioDonwloadFileDto.getInputStream()));
    }

}
