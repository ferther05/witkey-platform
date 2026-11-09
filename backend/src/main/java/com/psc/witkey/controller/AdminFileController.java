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

    @GetMapping
    public Result<PageResult<FileVO>> page(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(fileService.page(page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.ok();
    }
}
