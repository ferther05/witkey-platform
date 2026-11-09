package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.BizOrder;
import com.psc.witkey.domain.FileResource;
import com.psc.witkey.dto.FileVO;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.FileResourceRepository;
import com.psc.witkey.storage.FileStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 文件服务：上传、预览/下载鉴权、后台文件管理。
 * 业务表只存 fileId，前端拿到的 url 是 /api/file/{id}/preview，永远不暴露真实路径。
 */
@Service
public class FileService {

    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "zip", "txt",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx");
    private static final Set<String> ALLOWED_BIZ = Set.of("AVATAR", "TASK", "DELIVER", "REPORT");
    private static final long MAX_SIZE = 20L * 1024 * 1024;

    private final FileResourceRepository fileRepository;
    private final BizOrderRepository orderRepository;
    private final FileStorage storage;

    public FileService(FileResourceRepository fileRepository, BizOrderRepository orderRepository,
                       FileStorage storage) {
        this.fileRepository = fileRepository;
        this.orderRepository = orderRepository;
        this.storage = storage;
    }

    @Transactional
    public FileVO upload(Long userId, MultipartFile multipart, String bizType, Long bizId) {
        if (multipart == null || multipart.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String biz = bizType == null || bizType.isBlank() ? "TASK" : bizType.toUpperCase(Locale.ROOT);
        if (!ALLOWED_BIZ.contains(biz)) {
            throw new BusinessException("不支持的业务类型");
        }
        if (multipart.getSize() > MAX_SIZE) {
            throw new BusinessException("文件不能超过 20MB");
        }
        String original = multipart.getOriginalFilename() == null ? "file" : multipart.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("不支持的文件类型：" + (ext.isEmpty() ? "无后缀" : ext));
        }

        byte[] bytes;
        try {
            bytes = multipart.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取上传文件失败");
        }

        String key = storage.store(bytes, original, multipart.getContentType());

        FileResource file = new FileResource();
        file.setOriginalName(original);
        file.setObjectKey(key);
        file.setBucket("local");
        file.setSize((long) bytes.length);
        file.setContentType(multipart.getContentType());
        file.setUploaderId(userId);
        file.setBizType(biz);
        file.setBizId(bizId);
        return FileVO.from(fileRepository.save(file));
    }

    public FileResource get(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
    }

    /**
     * 预览/下载鉴权：
     * 头像和任务附件对所有人可见；交付附件只有订单双方能看；举报证据只有提交人和管理员能看。
     */
    public void checkRead(FileResource file, Long userId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        String biz = file.getBizType() == null ? "" : file.getBizType();
        switch (biz) {
            case "AVATAR", "TASK" -> {
                return;
            }
            case "DELIVER" -> {
                BizOrder order = orderRepository.findById(file.getBizId()).orElse(null);
                if (order == null) {
                    throw new BusinessException(404, "文件不存在");
                }
                if (userId != null && (userId.equals(order.getEmployerId()) || userId.equals(order.getWitkeyId()))) {
                    return;
                }
                throw new BusinessException(403, "无权查看该文件");
            }
            case "REPORT" -> {
                if (userId != null && userId.equals(file.getUploaderId())) {
                    return;
                }
                throw new BusinessException(403, "无权查看该文件");
            }
            default -> throw new BusinessException(403, "无权查看该文件");
        }
    }

    public byte[] read(FileResource file) {
        return storage.load(file.getObjectKey());
    }

    public List<FileVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return fileRepository.findByIdIn(ids).stream().map(FileVO::from).toList();
    }

    public PageResult<FileVO> page(int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        Page<FileResource> result = fileRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(p - 1, s));
        return new PageResult<>(result.getContent().stream().map(FileVO::from).toList(),
                result.getTotalElements(), p, s);
    }

    @Transactional
    public void delete(Long id) {
        FileResource file = get(id);
        storage.delete(file.getObjectKey());
        fileRepository.delete(file);
    }
}
