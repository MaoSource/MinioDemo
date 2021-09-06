package com.source.controller;

import com.source.configure.MinioTemplate;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Source
 * @date 2021/07/27/10:18
 * @Description:
 */
@RestController
@Slf4j
@RequestMapping("/minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioTemplate minioTemplate;

    @Value("${minio.bucketName}")
    private String bucketName;

    @PutMapping
    public String uploadFile(String bucketName, MultipartFile file) {
        try {
            if (StringUtils.isBlank(bucketName)){
                bucketName = this.bucketName;
            }
            String originalFilename = file.getOriginalFilename();
            log.info(originalFilename);
            return minioTemplate.putObject(bucketName,originalFilename,file);
        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败";
        }
    }

    @GetMapping
    public String getFile(String objectName) {
        try {
            String objectURL = minioTemplate.getObjectURL(bucketName, objectName);
            return objectURL;
        } catch (Exception e) {
            e.printStackTrace();
            return "获取失败";
        }
    }

    @DeleteMapping
    public String deleteFile(String objectName) {
        try {
            minioTemplate.removeObject(bucketName, objectName);
            return "删除成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败";
        }
    }

    @GetMapping("/setPolicy")
    public String setBucketPolicy(String bucketName) {
        try {
            minioTemplate.setBucketPolicy(bucketName);
            return "设置成功";
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (XmlParserException e) {
            e.printStackTrace();
        }
        return "设置失败";
    }
}
