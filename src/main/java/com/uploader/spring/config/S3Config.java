package com.uploader.spring.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.uploader.spring.utils.constant.ApiBeanConstant;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    // Biznet Property
    @Value("${biznet.s3.access-key}")
    private String accessKey;

    @Value("${biznet.s3.secret-key}")
    private String secretKey;

    @Value("${biznet.s3.endpoint}")
    private String endpoint;

    @Value("${biznet.s3.region}")
    private String region;

    // Minio Property
    @Value("${minio.s3.access-key}")
    private String minioAccessKey;

    @Value("${minio.s3.secret-key}")
    private String minioSecretKey;

    @Value("${minio.s3.endpoint}")
    private String minioEndpoint;

    @Value("${minio.s3.region}")
    private String minioRegion;

    @Bean(name = ApiBeanConstant.BIZNETS3)
    S3Client biznetS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();
    }

    @Bean(name = ApiBeanConstant.MINIOS3)
    S3Client minioS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(this.minioEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(this.minioAccessKey, this.minioSecretKey)))
                .region(Region.of(this.minioRegion))
                .forcePathStyle(true)
                .build();
    }

}
