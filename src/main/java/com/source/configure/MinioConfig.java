package com.source.configure;

import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Source
 * @date 2021/07/27/10:02
 * @Description:
 */
@Configuration
public class MinioConfig {

    @Bean
    public MinioTemplate minioTemplate(@Value("${minio.endPoint}") String endPoint, @Value("${minio.accessKey}") String accessKey,
                                       @Value("${minio.secretKey}") String secretKey, @Value("${minio.partSize}") int partSize) throws MinioException {
        return new MinioTemplate(endPoint, accessKey, secretKey, partSize);
    }
}
