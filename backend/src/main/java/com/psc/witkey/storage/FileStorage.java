// FileStorage —— 存储抽象接口：屏蔽本地磁盘与对象存储差异
package com.psc.witkey.storage;

/**
 * 文件存储抽象：本地磁盘、MinIO、阿里云 OSS 都是它的实现。
 * 业务层只依赖这个接口，换存储不改业务代码——这是模块五的设计重点。
 */
public interface FileStorage {

    /** 保存文件，返回对象键（本地实现是相对路径，OSS 实现是 objectKey） */
    String store(byte[] bytes, String originalName, String contentType);

    /** 读取文件内容 */
    byte[] load(String objectKey);

    // 删除
    void delete(String objectKey);

    // 判断文件是否存在
    boolean exists(String objectKey);

    /** 存储位置标识：本地实现为 local，对象存储实现为桶名（写入 file_resource.bucket） */
    default String bucketName() {
        return "local";
    }
}
