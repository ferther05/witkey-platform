// LocalFileStorage —— 存储实现：把文件写到本地磁盘目录
package com.psc.witkey.storage;

import com.psc.witkey.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 本地磁盘实现：把文件按 yyyy/MM/uuid.ext 存到配置目录。
 * 生产环境换成 MinIO/OSS 实现即可，业务代码不用动。
 */
@Component
@ConditionalOnProperty(name = "witkey.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(@Value("${witkey.storage.root:./uploads}") String root) {
        this.root = Paths.get(root).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建文件存储目录: " + this.root, e);
        }
    }

    // 写入文件内容，返回存储相对路径
    @Override
    // 写入文件
    public String store(byte[] bytes, String originalName, String contentType) {
        String ext = "";
        int dot = originalName == null ? -1 : originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot).toLowerCase();
        }
        LocalDate now = LocalDate.now();
        String key = String.format("%d/%02d/%s%s", now.getYear(), now.getMonthValue(),
                UUID.randomUUID().toString().replace("-", ""), ext);
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("非法的文件路径");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
            return key;
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败");
        }
    }

    // 读取文件内容
    @Override
    // 读取文件
    public byte[] load(String objectKey) {
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("非法的文件路径");
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new BusinessException(404, "文件不存在或已被清理");
        }
    }

    // 删除
    @Override
    // 删除
    public void delete(String objectKey) {
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 删除失败不影响业务，后台可再清理
        }
    }

    // 判断文件是否存在
    @Override
    // 判断存在
    public boolean exists(String objectKey) {
        Path target = root.resolve(objectKey).normalize();
        return target.startsWith(root) && Files.exists(target);
    }
}
