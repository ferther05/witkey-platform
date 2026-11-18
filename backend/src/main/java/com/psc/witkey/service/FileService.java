// FileService —— 业务：文件上传校验、读取鉴权、后台文件管理
package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.BizOrder;
import com.psc.witkey.domain.FileResource;
import com.psc.witkey.dto.FileVO;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.FileResourceRepository;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.repository.TaskRepository;
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
    private final TaskRepository taskRepository;
    private final BizOrderRepository orderRepository;
    private final SysUserRepository userRepository;
    private final FileStorage storage;

    public FileService(FileResourceRepository fileRepository,
                       TaskRepository taskRepository,
                       BizOrderRepository orderRepository,
                       SysUserRepository userRepository,
                       FileStorage storage) {
        this.fileRepository = fileRepository;
        this.taskRepository = taskRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.storage = storage;
    }

    // 上传文件：校验扩展名与大小并落盘
    @Transactional
    // 上传文件
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
        file.setBucket(storage.bucketName());
        file.setSize((long) bytes.length);
        file.setContentType(multipart.getContentType());
        file.setUploaderId(userId);
        file.setBizType(biz);
        file.setBizId(bizId);
        return FileVO.from(fileRepository.save(file));
    }

    // 获取
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

    // 读取文件字节内容
    public byte[] read(FileResource file) {
        return storage.load(file.getObjectKey());
    }

    // 按 id 集合查询
    public List<FileVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return fileRepository.findByIdIn(ids).stream().map(FileVO::from).toList();
    }

    // 分页查询
    public PageResult<FileVO> page(String bizType, String keyword,
                                   java.time.LocalDateTime start, java.time.LocalDateTime end,
                                   int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        var spec = (org.springframework.data.jpa.domain.Specification<FileResource>) (root, query, cb) -> {
            var ps = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (bizType != null && !bizType.isBlank()) {
                ps.add(cb.equal(root.get("bizType"), bizType.trim().toUpperCase()));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                ps.add(cb.or(cb.like(root.get("originalName"), like),
                        cb.like(root.get("objectKey"), like)));
            }
            if (start != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<FileResource> result = fileRepository.findAll(spec, PageRequest.of(p - 1, s,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));

        // 补上传人昵称
        var uploaderIds = result.getContent().stream().map(FileResource::getUploaderId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        var names = userRepository.findAllById(uploaderIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.psc.witkey.domain.SysUser::getId,
                        u -> u.getNickname() == null || u.getNickname().isBlank()
                                ? u.getUsername() : u.getNickname()));

        var list = result.getContent().stream().map(f -> {
            FileVO vo = FileVO.from(f);
            return new FileVO(vo.id(), vo.name(), vo.size(), vo.contentType(), vo.bizType(),
                    vo.bizId(), vo.uploaderId(), vo.url(), vo.image(), vo.createdAt(),
                    names.getOrDefault(f.getUploaderId(), "未知用户"),
                    (f.getBucket() == null ? "local" : f.getBucket()) + " / " + f.getObjectKey());
        }).toList();
        return new PageResult<>(list, result.getTotalElements(), p, s);
    }

    // 删除
    @Transactional
    // 删除
    public void delete(Long id) {
        FileResource file = get(id);
        // 被任务附件 / 交付附件 / 头像引用时不允许删除，避免业务数据出现死链
        String fid = String.valueOf(id);
        long refs = taskRepository.countByFileRef(fid)
                + orderRepository.countByDeliverFileRef(fid)
                + userRepository.countByAvatarContaining("/api/file/" + fid + "/");
        if (refs > 0) {
            throw new BusinessException("该文件正被 " + refs + " 处引用（任务附件/交付附件/头像），请先解除引用再删除");
        }
        storage.delete(file.getObjectKey());
        fileRepository.delete(file);
    }
}
