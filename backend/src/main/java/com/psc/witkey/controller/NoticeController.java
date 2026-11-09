package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.domain.Notice;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public Result<PageResult<Notice>> page(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(noticeService.page(SecurityUtils.userId(), page, size));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.ok(Map.of("count", noticeService.unreadCount(SecurityUtils.userId())));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(SecurityUtils.userId(), id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        noticeService.markAllRead(SecurityUtils.userId());
        return Result.ok();
    }
}
