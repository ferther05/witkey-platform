// FileController —— 接口：文件上传、元信息、预览、下载
package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.domain.FileResource;
import com.psc.witkey.dto.FileVO;
import com.psc.witkey.security.LoginUser;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 接口：文件上传、元信息、预览、下载。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 上传文件：校验扩展名与大小并落盘
    @PostMapping("/upload")
    // 上传文件
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(defaultValue = "TASK") String bizType,
                                 @RequestParam(required = false) Long bizId) {
        var vo = fileService.upload(SecurityUtils.userId(), file, bizType, bizId);
        // 操作留痕：上传文件
        com.psc.witkey.service.OpLog.now("FILE_UPLOAD", "FILE", vo.id(), "上传文件「" + vo.name() + "」用途 " + vo.bizType());
        return Result.ok(vo);
    }

    // 查询文件元信息
    @GetMapping("/{id}")
    // 执行 meta
    public Result<FileVO> meta(@PathVariable Long id) {
        LoginUser user = SecurityUtils.currentOrNull();
        FileResource file = fileService.get(id);
        fileService.checkRead(file, user == null ? null : user.id(), user != null && "ADMIN".equals(user.role()));
        return Result.ok(FileVO.from(file));
    }

    /** 在线预览：图片/PDF 直接在浏览器打开，其他类型浏览器会当下载 */
    @GetMapping("/{id}/preview")
    // 在线预览
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        LoginUser user = SecurityUtils.currentOrNull();
        FileResource file = fileService.get(id);
        fileService.checkRead(file, user == null ? null : user.id(), user != null && "ADMIN".equals(user.role()));
        return build(file, true);
    }

    /** 下载：强制附件下载，文件名用原始名 */
    @GetMapping("/{id}/download")
    // 下载（鉴权）
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        LoginUser user = SecurityUtils.currentOrNull();
        FileResource file = fileService.get(id);
        fileService.checkRead(file, user == null ? null : user.id(), user != null && "ADMIN".equals(user.role()));
        return build(file, false);
    }

    // 构建查询条件
    private ResponseEntity<byte[]> build(FileResource file, boolean inline) {
        byte[] bytes = fileService.read(file);
        String type = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
        String disposition = (inline ? "inline" : "attachment") + "; filename*=UTF-8''"
                + URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(type))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(bytes);
    }
}
