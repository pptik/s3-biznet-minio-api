package com.uploader.spring.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import java.awt.image.BufferedImage;

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
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
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

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/jpeg") ||
                contentType.startsWith("image/png") ||
                contentType.startsWith("image/gif") ||
                contentType.startsWith("image/bmp"))) {
            throw new IOException(
                    "Unsupported file type. Only image file (JPEG, PNG, GIF, BMP) are allowed for WebP conversion.");
        }

        String originalFilename = file.getOriginalFilename();
        String baseFilename = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        } else if (originalFilename != null) {
            baseFilename = originalFilename;
        } else {
            baseFilename = "untitled";
        }

        String outputFormat;
        String outputMimeType;
        String outputExtension;

        if (contentType.startsWith("image/jpeg")) {
            outputFormat = "jpeg";
            outputMimeType = "image/jpeg";
            outputExtension = ".jpg";
        } else if (contentType.startsWith("image/png")) {
            outputFormat = "png";
            outputMimeType = "image/png";
            outputExtension = ".png";
        } else if (contentType.startsWith("image/gif")) {
            outputFormat = "gif";
            outputMimeType = "image/gif";
            outputExtension = ".gif";
        } else if (contentType.startsWith("image/bmp")) {
            outputFormat = "bmp";
            outputMimeType = "image/bmp";
            outputExtension = ".bmp";
        } else {
            log.warn("Original content type {} not directly mapped for output. Defaulting to JPEG.",
                    contentType);
            outputFormat = "jpeg";
            outputMimeType = "image/jpeg";
            outputExtension = ".jpg";
        }

        String s3Key = baseFilename + "-" + UUID.randomUUID().toString().substring(0, 8) + outputExtension;

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            if (originalImage == null) {
                throw new IOException("Could not read image data from the provided file.");
            }

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(outputMimeType);

            if (!writers.hasNext()) {
                throw new IOException(
                        "ImageWriter for " + outputFormat
                                + " not found. Ensure Java ImageIO has proper codec support.");
            }

            ImageWriter writer = writers.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();

            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(0.01f);
            }

            writer.setOutput(ImageIO.createImageOutputStream(os));
            writer.write(null, new IIOImage(originalImage, null, null), writeParam);
            writer.dispose();

            byte[] webpBytes = os.toByteArray();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(s3Key)
                    .contentType(outputMimeType)
                    .contentLength((long) webpBytes.length)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            PutObjectResponse putObjectResponse = this.s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(webpBytes));

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
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                log.error("Error closing ByteArrayOutputStream: {}", e.getMessage());
            }
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
