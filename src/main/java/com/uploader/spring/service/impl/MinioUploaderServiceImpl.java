package com.uploader.spring.service.impl;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uploader.spring.models.dto.MinioDonwloadFileDto;
import com.uploader.spring.models.dto.UploaderResponsedto;
import com.uploader.spring.service.ServeMinioService;
import com.uploader.spring.service.UploaderService;
import com.uploader.spring.utils.constant.ApiBeanConstant;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service(ApiBeanConstant.MINIOS3SERVICE)
@Slf4j
public class MinioUploaderServiceImpl implements UploaderService, ServeMinioService {

    @Autowired
    @Qualifier(ApiBeanConstant.MINIOS3)
    private S3Client s3Client;

    @Value("${minio.s3.bucket}")
    private String bucket;

    @Value("${minio.s3.endpoint}")
    private String endpoint;

    @Override
    public UploaderResponsedto uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("FIle Cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String s3Key = UUID.randomUUID().toString() + fileExtension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            PutObjectResponse putObjectResponse = this.s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File Uploaded successfully to s3. ETag: {}", putObjectResponse.eTag());

            String urlResponse = String.format("%s/%s",
                    "https://s3.lskk.co.id/api/v1/serve",
                    s3Key);

            return UploaderResponsedto.builder().url(urlResponse).build();
        } catch (S3Exception e) {
            log.error("S3 Upload failed", e.awsErrorDetails().errorMessage());
            throw new IOException("Failed to upload file to S3: " + e.getMessage());
        } catch (IOException e) {
            log.error("File Processing Error", e.getMessage());
            throw new IOException("Failed to read file content: " + e.getMessage());
        }
    }

    public MinioDonwloadFileDto serveFile(String s3Key) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = this.s3Client.getObject(getObjectRequest);

            GetObjectResponse response = s3Object.response();

            return MinioDonwloadFileDto.builder()
                    .inputStream(s3Object)
                    .contentType(response.contentType())
                    .contentLength(response.contentLength())
                    .build();

        } catch (S3Exception e) {
            log.error("MinIO serve failed for key {}:{}", s3Key, e.awsErrorDetails().errorMessage(), e);
            throw new IOException("Failed to serve file from MinIO: " + e.getMessage());
        }
    }
}
