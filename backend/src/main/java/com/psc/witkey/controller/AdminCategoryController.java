// AdminCategoryController —— 后台接口：任务分类的增删改查（含佣金比例）
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

    // 查询列表
    @GetMapping
    // 查询列表
    public Result<List<CategoryVO>> list() {
        return Result.ok(categoryService.listAll());
    }

    // 新建
    @PostMapping
    // 新建
    public Result<CategoryVO> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        java.math.BigDecimal rate = body.get("commissionRate") == null ? null
                : new java.math.BigDecimal(body.get("commissionRate").toString());
        Integer sort = body.get("sort") == null ? null : Integer.valueOf(body.get("sort").toString());
        var vo = categoryService.create(name, rate, sort);
        // 操作留痕：新增分类
        com.psc.witkey.service.OpLog.now("CATEGORY_CREATE", "CATEGORY", vo.id(), "新增分类「" + vo.name() + "」佣金 " + vo.commissionRate() + "%");
        return Result.ok(vo);
    }

    // 更新任务：重新回到待审核并调整托管金额
    @PutMapping("/{id}")
    // 更新数据
    public Result<CategoryVO> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        java.math.BigDecimal rate = body.get("commissionRate") == null ? null
                : new java.math.BigDecimal(body.get("commissionRate").toString());
        Integer sort = body.get("sort") == null ? null : Integer.valueOf(body.get("sort").toString());
        Integer status = body.get("status") == null ? null : Integer.valueOf(body.get("status").toString());
        var vo = categoryService.update(id, name, rate, sort, status);
        // 操作留痕：修改分类
        com.psc.witkey.service.OpLog.now("CATEGORY_UPDATE", "CATEGORY", id, "修改分类「" + vo.name() + "」佣金 " + vo.commissionRate() + "%");
        return Result.ok(vo);
    }

    // 删除
    @DeleteMapping("/{id}")
    // 删除
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        // 操作留痕：删除分类
        com.psc.witkey.service.OpLog.now("CATEGORY_DELETE", "CATEGORY", id, "删除分类 #" + id);
        return Result.ok();
    }
}
