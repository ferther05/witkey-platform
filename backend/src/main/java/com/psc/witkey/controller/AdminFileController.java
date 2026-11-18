// AdminFileController —— 后台接口：文件列表与删除
package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.dto.FileVO;
import com.psc.witkey.service.FileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台文件信息管理（工作项 5.3）。
 */
@RestController
@RequestMapping("/api/admin/file")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFileController {

    private final FileService fileService;

    public AdminFileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 分页查询
    @GetMapping
    // 分页查询
    public Result<PageResult<FileVO>> page(
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime start,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(fileService.page(bizType, keyword, start, end, page, size));
    }

    // 删除
    @DeleteMapping("/{id}")
    // 删除
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        com.psc.witkey.service.OpLog.now("FILE_DELETE", "FILE", id, "删除文件 #" + id);
        return Result.ok();
    }
}
