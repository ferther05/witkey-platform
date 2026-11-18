// NoticeController —— 接口：站内消息列表、未读数、标记已读
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

/**
 * 接口：站内消息列表、未读数、标记已读。
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 分页查询
    @GetMapping
    // 分页查询
    public Result<PageResult<Notice>> page(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(noticeService.page(SecurityUtils.userId(), page, size));
    }

    // 查询未读消息数
    @GetMapping("/unread-count")
    // 未读数
    public Result<Map<String, Long>> unreadCount() {
        return Result.ok(Map.of("count", noticeService.unreadCount(SecurityUtils.userId())));
    }

    // 标记单条消息已读
    @PutMapping("/{id}/read")
    // 标记已读
    public Result<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(SecurityUtils.userId(), id);
        return Result.ok();
    }

    // 全部标记已读
    @PutMapping("/read-all")
    // 执行 markAllRead
    public Result<Void> markAllRead() {
        noticeService.markAllRead(SecurityUtils.userId());
        return Result.ok();
    }
}
