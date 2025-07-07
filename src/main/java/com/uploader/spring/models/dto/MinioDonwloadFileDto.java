package com.uploader.spring.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinioDonwloadFileDto {

    private ResponseInputStream<GetObjectResponse> inputStream;
    private String contentType;
    private Long contentLength;

}
