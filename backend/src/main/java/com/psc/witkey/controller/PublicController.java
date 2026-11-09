package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.PublicOverviewVO;
import com.psc.witkey.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 游客可访问的公开数据（首页展示用），不含用户隐私。
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final StatsService statsService;

    public PublicController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public Result<PublicOverviewVO> overview() {
        return Result.ok(statsService.overview());
    }
}
