package com.psc.witkey.repository;

import com.psc.witkey.domain.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<SysUser> findByUsernameContainingOrNicknameContainingOrderByCreatedAtDesc(
            String username, String nickname, Pageable pageable);

    Page<SysUser> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
