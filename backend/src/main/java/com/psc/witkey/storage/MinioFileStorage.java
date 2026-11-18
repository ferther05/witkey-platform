// MinioFileStorage —— 存储实现：MinIO（S3 兼容）
package com.psc.witkey.storage;

import com.psc.witkey.common.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 存储实现：MinIO（S3 兼容）。通过 witkey.storage.type=minio 启用。
 * 对象键格式 yyyy/MM/uuid.ext，桶默认私有，前端仍通过后端接口鉴权读取。
 */
@Service
@ConditionalOnProperty(name = "witkey.storage.type", havingValue = "minio")
public class MinioFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(MinioFileStorage.class);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    private final MinioClient client;
    private final String bucket;

    public MinioFileStorage(@Value("${witkey.storage.minio.endpoint:http://127.0.0.1:9000}") String endpoint,
                            @Value("${witkey.storage.minio.access-key:}") String accessKey,
                            @Value("${witkey.storage.minio.secret-key:}") String secretKey,
                            @Value("${witkey.storage.minio.bucket:witkey}") String bucket) {
        this.bucket = bucket;
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        ensureBucket();
    }

    // 保存文件，返回对象键
    @Override
    // 写入文件
    public String store(byte[] bytes, String originalName, String contentType) {
        String key = LocalDate.now().format(MONTH) + "/"
                + UUID.randomUUID().toString().replace("-", "") + extOf(originalName);
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, bytes.length, -1)
                    .contentType(contentType == null || contentType.isBlank()
                            ? "application/octet-stream" : contentType)
                    .build());
            log.info("文件已上传 MinIO: {}/{} ({} bytes)", bucket, key, bytes.length);
            return key;
        } catch (Exception e) {
            log.error("上传 MinIO 失败", e);
            throw new BusinessException("文件上传到对象存储失败：" + e.getMessage());
        }
    }

    // 读取文件内容
    @Override
    // 读取文件
    public byte[] load(String objectKey) {
        try (var stream = client.getObject(GetObjectArgs.builder()
                .bucket(bucket).object(objectKey).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new BusinessException(404, "文件不存在");
        }
    }

    // 删除对象
    @Override
    // 删除
    public void delete(String objectKey) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            log.warn("删除 MinIO 对象失败: {}/{} - {}", bucket, objectKey, e.getMessage());
        }
    }

    // 判断对象是否存在
    @Override
    // 判断存在
    public boolean exists(String objectKey) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 桶名（写入 file_resource.bucket）
    @Override
    // 执行 bucketName
    public String bucketName() {
        return bucket;
    }

    // 启动时确保桶存在，避免手工漏建
    private void ensureBucket() {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("已创建 MinIO 桶: {}", bucket);
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 桶初始化失败（检查 endpoint / 密钥）: " + e.getMessage(), e);
        }
    }

    // 取扩展名
    private String extOf(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i).toLowerCase();
    }
}
