package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.domain.TaskCategory;
import com.psc.witkey.dto.CategoryVO;
import com.psc.witkey.repository.TaskCategoryRepository;
import com.psc.witkey.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分类维护：前台只读启用中的分类，后台可增删改。
 */
@Service
public class CategoryService {

    private final TaskCategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    public CategoryService(TaskCategoryRepository categoryRepository, TaskRepository taskRepository) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
    }

    public List<CategoryVO> listEnabled() {
        return categoryRepository.findByStatusOrderBySortAsc(1).stream().map(CategoryVO::from).toList();
    }

    public List<CategoryVO> listAll() {
        return categoryRepository.findAllByOrderBySortAsc().stream().map(CategoryVO::from).toList();
    }

    @Transactional
    public CategoryVO create(String name, BigDecimal commissionRate, Integer sort) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("分类名称不能为空");
        }
        if (categoryRepository.existsByName(name.trim())) {
            throw new BusinessException("分类已存在");
        }
        TaskCategory category = new TaskCategory();
        category.setName(name.trim());
        category.setCommissionRate(commissionRate == null ? new BigDecimal("10.00") : commissionRate);
        category.setSort(sort == null ? 0 : sort);
        return CategoryVO.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryVO update(Long id, String name, BigDecimal commissionRate, Integer sort, Integer status) {
        TaskCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        if (name != null && !name.isBlank()) {
            category.setName(name.trim());
        }
        if (commissionRate != null) {
            if (commissionRate.compareTo(BigDecimal.ZERO) < 0 || commissionRate.compareTo(new BigDecimal("50")) > 0) {
                throw new BusinessException("佣金比例需在 0-50 之间");
            }
            category.setCommissionRate(commissionRate);
        }
        if (sort != null) {
            category.setSort(sort);
        }
        if (status != null) {
            category.setStatus(status);
        }
        return CategoryVO.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        TaskCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        // 简化规则：分类下已有任务时不物理删除，改为停用，避免任务失去分类
        if (taskRepository.countByCategoryId(id) > 0) {
            category.setStatus(0);
            categoryRepository.save(category);
            throw new BusinessException("该分类下已有任务，已改为停用而不是删除");
        }
        categoryRepository.delete(category);
    }
}
