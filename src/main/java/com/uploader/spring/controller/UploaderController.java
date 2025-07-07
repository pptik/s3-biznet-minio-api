package com.uploader.spring.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uploader.spring.models.dto.ResponseWrapper;
import com.uploader.spring.models.dto.UploaderResponsedto;
import com.uploader.spring.service.UploaderService;
import com.uploader.spring.utils.constant.ApiBeanConstant;
import com.uploader.spring.utils.constant.ApiPathConstant;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping(ApiPathConstant.API +
                ApiPathConstant.VERSION +
                "/uploader")
public class UploaderController {

        @Autowired
        @Qualifier(ApiBeanConstant.BIZNETS3SERVICE)
        private UploaderService biznetUploader;

        @Autowired
        @Qualifier(ApiBeanConstant.MINIOS3SERVICE)
        private UploaderService minioUploader;

        @PostMapping(value = "s3/biznet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ResponseWrapper<UploaderResponsedto>> uploaderBiznetS3Handler(
                        @RequestParam("file") MultipartFile file) throws IOException {

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                new ResponseWrapper<UploaderResponsedto>(
                                                                HttpStatus.OK.value(),
                                                                Boolean.TRUE,
                                                                "Success Upload data",
                                                                this.biznetUploader.uploadFile(file)));
        }

        @PostMapping(value = "s3/minio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ResponseWrapper<UploaderResponsedto>> uploaderMinioS3Handler(
                        @RequestParam("file") MultipartFile file) throws IOException {
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                new ResponseWrapper<UploaderResponsedto>(
                                                                HttpStatus.OK.value(),
                                                                Boolean.TRUE,
                                                                "Success Upload Data Minio",
                                                                this.minioUploader.uploadFile(file)));
        }

}
