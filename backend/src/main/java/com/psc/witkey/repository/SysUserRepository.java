// SysUserRepository —— 数据访问：用户表
package com.psc.witkey.repository;

import com.psc.witkey.domain.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 数据访问：用户表。
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    // 按条件查询
    Optional<SysUser> findByUsername(String username);

    // 用户名是否已存在
    boolean existsByUsername(String username);

    Page<SysUser> findByUsernameContainingOrNicknameContainingOrderByCreatedAtDesc(
            String username, String nickname, Pageable pageable);

    // 查询
    Page<SysUser> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 统计有多少用户把该文件当作头像
    long countByAvatarContaining(String part);
}
