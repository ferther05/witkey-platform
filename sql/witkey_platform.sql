-- =============================================================
-- 威客任务平台 数据库初始化脚本  witkey_platform
-- MySQL 8.0 / utf8mb4
-- 说明：模块一（用户与权限管理）的两张表由 JPA 生成，此处保持完全一致，
--       其余表为后续模块预建，脚本可重复执行。
-- 执行：mysql -u root -p --default-character-set=utf8mb4 < witkey_platform.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS `witkey_platform`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `witkey_platform`;

-- -------------------------------------------------------------
-- 1. 用户（账号 + 资料 + 身份，项目规模不大不拆资料表）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `username`      VARCHAR(50)  NOT NULL COMMENT '登录名',
    `password`      VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    `nickname`      VARCHAR(50)  DEFAULT NULL,
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `intro`         VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    `skills`        VARCHAR(255) DEFAULT NULL COMMENT '技能标签，逗号分隔',
    `mobile`        VARCHAR(20)  DEFAULT NULL,
    `role`          VARCHAR(20)  NOT NULL COMMENT 'EMPLOYER/WITKEY/ADMIN',
    `identity_type` VARCHAR(20)  NOT NULL COMMENT '主身份 EMPLOYER/WITKEY',
    `auth_status`   VARCHAR(20)  NOT NULL COMMENT 'UNVERIFIED/PENDING/VERIFIED/REJECTED',
    `real_name`     VARCHAR(50)  DEFAULT NULL COMMENT '实名姓名',
    `status`        INT          NOT NULL DEFAULT 1 COMMENT '1 正常 0 封禁',
    `created_at`    DATETIME(6)  NOT NULL,
    `updated_at`    DATETIME(6)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- -------------------------------------------------------------
-- 2. 站内消息
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notice` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT        NOT NULL,
    `type`       VARCHAR(20)   NOT NULL COMMENT 'SYSTEM/TRADE',
    `title`      VARCHAR(100)  NOT NULL,
    `content`    VARCHAR(1000) NOT NULL,
    `biz_type`   VARCHAR(20)   DEFAULT NULL COMMENT 'TASK/ORDER/BID/USER',
    `biz_id`     BIGINT        DEFAULT NULL,
    `is_read`    BIT(1)        NOT NULL DEFAULT b'0',
    `created_at` DATETIME(6)   NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_notice_user_read` (`user_id`, `is_read`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='站内消息';

-- -------------------------------------------------------------
-- 3. 任务分类（含分类佣金比例）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `task_category` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(50)  NOT NULL,
    `parent_id`       BIGINT       NOT NULL DEFAULT 0,
    `sort`            INT          NOT NULL DEFAULT 0,
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
    `commission_rate` DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT '平台抽佣百分比',
    `created_at`      DATETIME(6)  NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='任务分类';

-- -------------------------------------------------------------
-- 4. 任务
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `task` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `title`         VARCHAR(100)  NOT NULL,
    `category_id`   BIGINT        NOT NULL,
    `employer_id`   BIGINT        NOT NULL,
    `description`   TEXT          NOT NULL,
    `budget`        DECIMAL(12,2) NOT NULL,
    `deadline`      DATETIME      NOT NULL,
    `status`        VARCHAR(20)   NOT NULL COMMENT 'PENDING_AUDIT/REJECTED/RECRUITING/SELECTED/IN_PROGRESS/TO_ACCEPT/DONE/WITHDRAWN/FLOWED',
    `audit_remark`  VARCHAR(255)  DEFAULT NULL,
    `bid_count`     INT           NOT NULL DEFAULT 0,
    `view_count`    INT           NOT NULL DEFAULT 0,
    `created_at`    DATETIME(6)   NOT NULL,
    `updated_at`    DATETIME(6)   NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_task_status_cat` (`status`, `category_id`, `created_at`),
    KEY `idx_task_employer` (`employer_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='任务';

-- -------------------------------------------------------------
-- 5. 任务收藏
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `task_favorite` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `task_id`    BIGINT      NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_task` (`user_id`, `task_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='任务收藏';

-- -------------------------------------------------------------
-- 6. 投标
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `bid` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `task_id`    BIGINT        NOT NULL,
    `witkey_id`  BIGINT        NOT NULL,
    `quote`      DECIMAL(12,2) NOT NULL COMMENT '报价',
    `content`    VARCHAR(2000) NOT NULL COMMENT '方案说明',
    `status`     VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/WIN/LOSE/WITHDRAWN',
    `created_at` DATETIME(6)   NOT NULL,
    `updated_at` DATETIME(6)   NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bid_task_witkey` (`task_id`, `witkey_id`),
    KEY `idx_bid_task_status` (`task_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='投标';

-- -------------------------------------------------------------
-- 7. 担保订单（选标后生成，含佣金快照）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_order` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `order_no`          VARCHAR(32)   NOT NULL,
    `task_id`           BIGINT        NOT NULL,
    `employer_id`       BIGINT        NOT NULL,
    `witkey_id`         BIGINT        NOT NULL,
    `amount`            DECIMAL(12,2) NOT NULL COMMENT '赏金',
    `commission_rate`   DECIMAL(5,2)  NOT NULL COMMENT '下单时佣金比例快照',
    `commission_amount` DECIMAL(12,2) NOT NULL,
    `status`            VARCHAR(20)   NOT NULL COMMENT 'TO_DELIVER/TO_ACCEPT/DONE/REFUNDED',
    `deliver_note`      VARCHAR(1000) DEFAULT NULL COMMENT '交付说明',
    `deliver_file_ids`  VARCHAR(500)  DEFAULT NULL COMMENT '交付附件 file_id，逗号分隔',
    `deliver_count`     INT           NOT NULL DEFAULT 0 COMMENT '交付版本数',
    `reject_reason`     VARCHAR(500)  DEFAULT NULL COMMENT '验收驳回原因',
    `deliver_time`      DATETIME      DEFAULT NULL,
    `accept_time`       DATETIME      DEFAULT NULL,
    `settle_time`       DATETIME      DEFAULT NULL,
    `created_at`        DATETIME(6)   NOT NULL,
    `updated_at`        DATETIME(6)   NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_order_task` (`task_id`),
    KEY `idx_order_employer` (`employer_id`),
    KEY `idx_order_witkey` (`witkey_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='担保订单';

-- -------------------------------------------------------------
-- 8. 钱包账户
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `wallet_account` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT        NOT NULL,
    `balance`    DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
    `frozen`     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结（托管中）',
    `updated_at` DATETIME(6)   NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wallet_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='钱包账户';

-- -------------------------------------------------------------
-- 9. 资金流水（唯一键 biz_no 保证结算/退款幂等）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `wallet_flow` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT        NOT NULL,
    `biz_no`        VARCHAR(64)   NOT NULL COMMENT '业务单号，唯一，用于幂等',
    `type`          VARCHAR(20)   NOT NULL COMMENT 'RECHARGE/FREEZE/UNFREEZE/SETTLE/COMMISSION/WITHDRAW',
    `amount`        DECIMAL(12,2) NOT NULL COMMENT '正数入账，负数出账',
    `balance_after` DECIMAL(12,2) NOT NULL,
    `remark`        VARCHAR(255)  DEFAULT NULL,
    `created_at`    DATETIME(6)   NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_biz_no` (`biz_no`),
    KEY `idx_flow_user_time` (`user_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='资金流水';

-- -------------------------------------------------------------
-- 10. 文件资源（OSS 对象存储元信息）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `file_resource` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `original_name` VARCHAR(255) NOT NULL,
    `object_key`    VARCHAR(255) NOT NULL COMMENT 'OSS 对象键',
    `bucket`        VARCHAR(64)  DEFAULT NULL,
    `size`          BIGINT       NOT NULL DEFAULT 0,
    `content_type`  VARCHAR(100) DEFAULT NULL,
    `uploader_id`   BIGINT       NOT NULL,
    `biz_type`      VARCHAR(20)  DEFAULT NULL COMMENT 'AVATAR/TASK/DELIVER/REPORT',
    `biz_id`        BIGINT       DEFAULT NULL,
    `created_at`    DATETIME(6)  NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_file_uploader` (`uploader_id`),
    KEY `idx_file_biz` (`biz_type`, `biz_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='文件资源';

-- -------------------------------------------------------------
-- 11. 举报（兼作纠纷兜底，可对订单执行退款/打款）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `report` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `reporter_id`         BIGINT       NOT NULL,
    `target_type`         VARCHAR(20)  NOT NULL COMMENT 'TASK/USER/ORDER',
    `target_id`           BIGINT       NOT NULL,
    `reason`              VARCHAR(500) NOT NULL,
    `file_ids`            VARCHAR(500) DEFAULT NULL COMMENT '证据文件 id，逗号分隔',
    `status`              VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE/REJECTED',
    `handle_remark`       VARCHAR(500) DEFAULT NULL,
    `handle_order_action` VARCHAR(20)  DEFAULT NULL COMMENT 'REFUND/PAY/NONE',
    `created_at`          DATETIME(6)  NOT NULL,
    `handled_at`          DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_report_status` (`status`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='举报';

-- -------------------------------------------------------------
-- 初始化数据
-- -------------------------------------------------------------
-- 管理员账号由后端 DataInitializer 在首次启动时创建（admin / 123456）
INSERT INTO `task_category` (`name`, `parent_id`, `sort`, `status`, `commission_rate`, `created_at`)
SELECT '设计与创意', 0, 1, 1, 10.00, NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `task_category` WHERE `name` = '设计与创意');

INSERT INTO `task_category` (`name`, `parent_id`, `sort`, `status`, `commission_rate`, `created_at`)
SELECT '开发与编程', 0, 2, 1, 8.00, NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `task_category` WHERE `name` = '开发与编程');

INSERT INTO `task_category` (`name`, `parent_id`, `sort`, `status`, `commission_rate`, `created_at`)
SELECT '文案与翻译', 0, 3, 1, 5.00, NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `task_category` WHERE `name` = '文案与翻译');
