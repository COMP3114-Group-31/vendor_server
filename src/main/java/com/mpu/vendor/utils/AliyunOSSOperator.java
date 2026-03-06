package com.mpu.vendor.utils;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.mpu.vendor.config.AliyunOSSProperties;

@Component
public class AliyunOSSOperator {
    private static final Logger log = LoggerFactory.getLogger(AliyunOSSOperator.class);

    private final AliyunOSSProperties aliyunOSSProperties;

    public AliyunOSSOperator(AliyunOSSProperties aliyunOSSProperties) {
        this.aliyunOSSProperties = aliyunOSSProperties;
    }

    public String uploadProductThumbnail(byte[] content, String originalFilename, Long productId) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory
                .newEnvironmentVariableCredentialsProvider();

        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = UUID.randomUUID() + ext;
        String objectName = "products/" + productId + "/thumbnails/" + dir + "/" + fileName;

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        log.info("OSS upload start: bucket={}, region={}, object={}", bucketName, region, objectName);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
            log.info("OSS upload success: object={}", objectName);
        } catch (Exception ex) {
            log.error("OSS upload failed: bucket={}, region={}, object={}, reason={}",
                    bucketName, region, objectName, ex.getMessage(), ex);
            throw ex;
        } finally {
            ossClient.shutdown();
        }

        String protocol = endpoint.split("//")[0];
        String host = endpoint.split("//")[1];
        return protocol + "//" + bucketName + "." + host + "/" + objectName;
    }
}
