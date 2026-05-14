package com.techmatch.resume.service;

import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import com.techmatch.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String upload(String objectKey, byte[] content, String contentType) {
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, content.length, -1)
                    .contentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
                    .build());
            return minioProperties.getEndpoint() + "/" + minioProperties.getBucket() + "/" + objectKey;
        } catch (Exception exception) {
            throw new BizException(ErrorCode.FILE_UPLOAD_FAILED.getCode(), exception.getMessage());
        }
    }

    public String getBucket() {
        return minioProperties.getBucket();
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        }
    }
}
