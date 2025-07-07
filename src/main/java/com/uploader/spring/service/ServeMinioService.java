package com.uploader.spring.service;

import java.io.IOException;

import com.uploader.spring.models.dto.MinioDonwloadFileDto;

public interface ServeMinioService {
    MinioDonwloadFileDto serveFile(String s3Key) throws IOException;
}
