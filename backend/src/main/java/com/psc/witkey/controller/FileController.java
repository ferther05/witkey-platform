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

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(defaultValue = "TASK") String bizType,
                                 @RequestParam(required = false) Long bizId) {
        return Result.ok(fileService.upload(SecurityUtils.userId(), file, bizType, bizId));
    }

    @GetMapping("/{id}")
    public Result<FileVO> meta(@PathVariable Long id) {
        LoginUser user = SecurityUtils.currentOrNull();
        FileResource file = fileService.get(id);
        fileService.checkRead(file, user == null ? null : user.id(), user != null && "ADMIN".equals(user.role()));
        return Result.ok(FileVO.from(file));
    }

    /** 在线预览：图片/PDF 直接在浏览器打开，其他类型浏览器会当下载 */
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        LoginUser user = SecurityUtils.currentOrNull();
        FileResource file = fileService.get(id);
        fileService.checkRead(file, user == null ? null : user.id(), user != null && "ADMIN".equals(user.role()));
        return build(file, true);
    }

    /** 下载：强制附件下载，文件名用原始名 */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        LoginUser user = SecurityUtils.currentOrNull();
        FileResource file = fileService.get(id);
        fileService.checkRead(file, user == null ? null : user.id(), user != null && "ADMIN".equals(user.role()));
        return build(file, false);
    }

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
