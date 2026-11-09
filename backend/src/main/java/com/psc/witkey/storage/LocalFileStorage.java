package com.psc.witkey.storage;

import com.psc.witkey.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public boolean exists(String objectKey) {
        Path target = root.resolve(objectKey).normalize();
        return target.startsWith(root) && Files.exists(target);
    }
}
