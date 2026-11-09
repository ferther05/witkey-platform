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

    void delete(String objectKey);

    boolean exists(String objectKey);
}
