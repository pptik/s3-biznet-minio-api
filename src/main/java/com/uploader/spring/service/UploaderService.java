package com.uploader.spring.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.uploader.spring.models.dto.UploaderResponsedto;

public interface UploaderService {
    UploaderResponsedto uploadFile(MultipartFile file) throws IOException;
}
