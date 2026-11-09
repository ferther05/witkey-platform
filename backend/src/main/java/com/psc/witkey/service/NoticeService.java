package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.Notice;
import com.psc.witkey.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 站内消息。其他模块产生业务动作时调用 push 即可（审核结果、中标、待验收、结算到账）。
 */
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public PageResult<Notice> page(Long userId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        Page<Notice> result = noticeRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(p - 1, s));
        return new PageResult<>(result.getContent(), result.getTotalElements(), p, s);
    }

    public long unreadCount(Long userId) {
        return noticeRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long noticeId) {
        Notice notice = noticeRepository.findByIdAndUserId(noticeId, userId)
                .orElseThrow(() -> new BusinessException(404, "消息不存在"));
        if (!Boolean.TRUE.equals(notice.getRead())) {
            notice.setRead(true);
            noticeRepository.save(notice);
        }
    }

    @Transactional
    public int markAllRead(Long userId) {
        return noticeRepository.markAllRead(userId);
    }

    @Transactional
    public void push(Long userId, String type, String title, String content, String bizType, Long bizId) {
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setBizType(bizType);
        notice.setBizId(bizId);
        notice.setRead(false);
        noticeRepository.save(notice);
    }
}
