// CategoryController —— 接口：前台读取任务分类
package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.CategoryVO;
import com.psc.witkey.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接口：前台读取任务分类。
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** 前台只取启用中的分类 */
    @GetMapping
    // 查询列表
    public Result<List<CategoryVO>> list() {
        return Result.ok(categoryService.listEnabled());
    }
}
