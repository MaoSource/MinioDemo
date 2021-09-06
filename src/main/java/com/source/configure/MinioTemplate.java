package com.source.configure;

import com.source.utils.FileNameUtil;
import com.source.utils.policyJsonUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Source
 * @date 2021/07/27/9:48
 * @Description:
 */
@AllArgsConstructor
@Slf4j
public class MinioTemplate {

    public String endpoint;
    public String accessKey;
    public String secretKey;
    public int partSize;

    public MinioClient client;

    @SneakyThrows
    public MinioTemplate(String endpoint, String accessKey, String secretKey, int partSize) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.partSize = partSize;
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

    }

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     */
    public void createBucket(String bucketName) throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 获取全部bucket
     * <p>
     * https://docs.minio.io/cn/java-client-api-reference.html#listBuckets
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return client.listBuckets();
    }

    /**
     * 根据bucketName获取信息
     * @param bucketName bucket名称
     */
    public Optional<Bucket> getBucket(String bucketName) throws Exception {
        return client.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
    }

    /**
     * 根据bucketName删除信息
     * @param bucketName bucket名称
     */
    public void removeBucket(String bucketName) throws Exception {
        client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 分区上传文件
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param stream 文件流
     * @param size 文件大小
     */
    public String putObject(String bucketName, String objectName, InputStream stream, Long size, String contentType) throws Exception{
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .contentType(contentType)
                .stream(stream, size, partSize)
                .build();
        ObjectWriteResponse objectWriteResponse = client.putObject(putObjectArgs);
        return objectWriteResponse.object();
    }

    /**
     * 根据文件前置查询文件
     *
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return MinioItem 列表
     */
    public List<Item> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive) throws Exception {
        List<Item> objectList = new ArrayList<>();
        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(recursive)
                .build();

        Iterable<Result<Item>> objectsIterator = client
                .listObjects(listObjectsArgs);

        while (objectsIterator.iterator().hasNext()) {
            objectList.add(objectsIterator.iterator().next().get());
        }
        return objectList;
    }

    /**
     * 获取文件外链
     * 这里的 method 方法决定最后链接是什么请求获得
     *  expiry 决定这个链接多久失效
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return url
     */
    public String getObjectURL(String bucketName, String objectName) throws Exception {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .method(Method.GET)
                .expiry(7, TimeUnit.DAYS)
                .object(objectName)
                .build();

        return client.getPresignedObjectUrl(args);
    }

    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();
        return client.getObject(getObjectArgs);
    }


    /**
     * 上传文件 base64
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param base64Str 文件base64
     */
    public String putObject(String bucketName, String objectName, String base64Str, String contentType) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(base64Str.getBytes());
        // 进行解码
        BASE64Decoder base64Decoder = new BASE64Decoder();
        byte[] byt = new byte[0];
        try {
            byt = base64Decoder.decodeBuffer(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStream = new ByteArrayInputStream(byt);
        putObject(bucketName, objectName, inputStream, Long.valueOf(byt.length), contentType);
        return objectName;
    }

    /**
     * 上传文件
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param file 文件
     * @throws Exception
     */
    public String putObject( String bucketName,String objectName, MultipartFile file) throws Exception{
        log.info("文件大小：" + file.getSize());
        this.idExists(bucketName);
        this.putObject(bucketName, objectName, file.getInputStream(), file.getSize(), file.getContentType());
        return objectName;
    }

    /**
     * 获取文件信息
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#statObject
     */
    public StatObjectResponse getObjectInfo(String bucketName, String objectName) throws Exception {
        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();
        return client.statObject(statObjectArgs);
    }

    /**
     * 删除文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#removeObject
     */
    public void removeObject(String bucketName, String objectName) throws Exception {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 根据文件名返回对应contentType
     * @param objectName
     * @return
     */
    private String getContentType(String objectName) {
        if(FileNameUtil.isPicture(objectName)) {
            return "image/jpeg";
        }
        if(FileNameUtil.isVideo(objectName)) {
            return "video/mp4";
        }
        return null;
    }

    /**
     * 获取直传链接
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @throws Exception
     */
    public String presignedPutObject( String bucketName,String objectName) throws Exception{
        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucketName)
                .object(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build();
        return client.getPresignedObjectUrl(getPresignedObjectUrlArgs);
    }

    /**
     * 合并文件
     * @param bucketName
     * @param chunkNames
     * @param targetObjectName
     * @return
     * @throws Exception
     */
    public String composeObject(String bucketName, List<String> chunkNames, String targetObjectName) throws Exception{

        List<ComposeSource> sources = new ArrayList<>(chunkNames.size());
        for (String chunkName : chunkNames) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucketName)
                    .object(chunkName)
                    .build();
            sources.add(composeSource);
        }

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketName)
                .sources(sources)
                .object(targetObjectName)
                .build();
        ObjectWriteResponse objectWriteResponse = client.composeObject(composeObjectArgs);
        return objectWriteResponse.object();
    }

    /**
     * 判断bucket是否存在
     * @param bucketName
     * @return
     */
    public void idExists(String bucketName) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            boolean found =
                    client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
                log.info("创建成功");
                setBucketPolicy(bucketName);
                log.info("设置策略成功");
            } else {
                log.info("存储桶:{}",bucketName  + "已存在！");
                System.out.println(bucketName + "存储桶已存在！");
            }
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }

    /**
     * 设置策略
     * @param bucketName 桶名
     * @throws ErrorResponseException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws ServerException
     * @throws XmlParserException
     */
    public void setBucketPolicy(String bucketName) throws ErrorResponseException, InsufficientDataException, InternalException,
    InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException,
    ServerException, XmlParserException {
        String s = policyJsonUtils.json2String("policyJson.json", bucketName);
        client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(s).build());
    }

}
