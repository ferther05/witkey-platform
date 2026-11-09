package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.CategoryVO;
import com.psc.witkey.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 后台分类维护（工作项 2.1 / 6.4 的同一套能力，后台是入口）。
 */
@RestController
@RequestMapping("/api/admin/category")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<CategoryVO>> list() {
        return Result.ok(categoryService.listAll());
    }

    @PostMapping
    public Result<CategoryVO> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        java.math.BigDecimal rate = body.get("commissionRate") == null ? null
                : new java.math.BigDecimal(body.get("commissionRate").toString());
        Integer sort = body.get("sort") == null ? null : Integer.valueOf(body.get("sort").toString());
        return Result.ok(categoryService.create(name, rate, sort));
    }

    @PutMapping("/{id}")
    public Result<CategoryVO> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        java.math.BigDecimal rate = body.get("commissionRate") == null ? null
                : new java.math.BigDecimal(body.get("commissionRate").toString());
        Integer sort = body.get("sort") == null ? null : Integer.valueOf(body.get("sort").toString());
        Integer status = body.get("status") == null ? null : Integer.valueOf(body.get("status").toString());
        return Result.ok(categoryService.update(id, name, rate, sort, status));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
