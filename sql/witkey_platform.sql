-- =============================================================
-- 威客任务平台 数据库初始化脚本  witkey_platform
-- MySQL 8.0 / utf8mb4
-- 说明：结构与后端 JPA 实体保持一致（12 张表），可重复执行。
-- 执行：mysql -u root -p --default-character-set=utf8mb4 < witkey_platform.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS `witkey_platform`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `witkey_platform`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `bid` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `witkey_id` bigint NOT NULL,
  `quote` decimal(12,2) NOT NULL COMMENT '报价',
  `content` varchar(2000) NOT NULL COMMENT '方案说明',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/WIN/LOSE/WITHDRAWN',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bid_task_witkey` (`task_id`,`witkey_id`),
  KEY `idx_bid_task_status` (`task_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投标';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `biz_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `task_id` bigint NOT NULL,
  `employer_id` bigint NOT NULL,
  `witkey_id` bigint NOT NULL,
  `amount` decimal(12,2) NOT NULL COMMENT '赏金',
  `commission_rate` decimal(5,2) NOT NULL COMMENT '下单时佣金比例快照',
  `commission_amount` decimal(12,2) NOT NULL,
  `status` varchar(20) NOT NULL COMMENT 'TO_DELIVER/TO_ACCEPT/DONE/REFUNDED',
  `deliver_time` datetime DEFAULT NULL,
  `accept_time` datetime DEFAULT NULL,
  `settle_time` datetime DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deliver_count` int NOT NULL,
  `deliver_note` varchar(1000) DEFAULT NULL,
  `reject_reason` varchar(500) DEFAULT NULL,
  `deliver_file_ids` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_order_task` (`task_id`),
  KEY `idx_order_employer` (`employer_id`),
  KEY `idx_order_witkey` (`witkey_id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='担保订单';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `file_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `original_name` varchar(255) NOT NULL,
  `object_key` varchar(255) NOT NULL COMMENT 'OSS 对象键',
  `bucket` varchar(64) DEFAULT NULL,
  `size` bigint NOT NULL DEFAULT '0',
  `content_type` varchar(100) DEFAULT NULL,
  `uploader_id` bigint NOT NULL,
  `biz_type` varchar(20) DEFAULT NULL COMMENT 'AVATAR/TASK/DELIVER/REPORT',
  `biz_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_file_uploader` (`uploader_id`),
  KEY `idx_file_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB AUTO_INCREMENT=185 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件资源';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `biz_id` bigint DEFAULT NULL,
  `biz_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `content` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notice_user_read` (`user_id`,`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=1209 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内消息';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(40) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `detail` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `role` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `target_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_log_created` (`created_at`),
  KEY `idx_log_user` (`user_id`),
  KEY `idx_log_action` (`action`)
) ENGINE=InnoDB AUTO_INCREMENT=803 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL,
  `target_type` varchar(20) NOT NULL COMMENT 'TASK/USER/ORDER',
  `target_id` bigint NOT NULL,
  `reason` varchar(500) NOT NULL,
  `file_ids` varchar(500) DEFAULT NULL COMMENT '证据文件 id，逗号分隔',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE/REJECTED',
  `handle_remark` varchar(500) DEFAULT NULL,
  `handle_order_action` varchar(20) DEFAULT NULL COMMENT 'REFUND/PAY/NONE',
  `created_at` datetime(6) NOT NULL,
  `handled_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_report_status` (`status`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='举报';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auth_status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `identity_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `intro` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `mobile` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `real_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `role` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `skills` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `auth_remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_51bvuyvihefoh4kp5syh2jpi4` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=220 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表（账号+资料+身份）';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `category_id` bigint NOT NULL,
  `employer_id` bigint NOT NULL,
  `description` text NOT NULL,
  `budget` decimal(12,2) NOT NULL,
  `deadline` datetime NOT NULL,
  `status` varchar(20) NOT NULL COMMENT 'PENDING_AUDIT/REJECTED/RECRUITING/SELECTED/IN_PROGRESS/TO_ACCEPT/DONE/WITHDRAWN/FLOWED',
  `audit_remark` varchar(255) DEFAULT NULL,
  `bid_count` int NOT NULL DEFAULT '0',
  `view_count` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `file_ids` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_task_status_cat` (`status`,`category_id`,`created_at`),
  KEY `idx_task_employer` (`employer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=273 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `task_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT '0',
  `sort` int NOT NULL DEFAULT '0',
  `status` int NOT NULL,
  `commission_rate` decimal(5,2) NOT NULL DEFAULT '10.00' COMMENT '平台抽佣百分比',
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务分类';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `task_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_task` (`user_id`,`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务收藏';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `wallet_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '可用余额',
  `frozen` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '冻结（托管中）',
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wallet_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包账户';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `wallet_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `biz_no` varchar(64) NOT NULL COMMENT '业务单号，唯一，用于幂等',
  `type` varchar(20) NOT NULL COMMENT 'RECHARGE/FREEZE/UNFREEZE/SETTLE/COMMISSION/WITHDRAW',
  `amount` decimal(12,2) NOT NULL COMMENT '正数入账，负数出账',
  `balance_after` decimal(12,2) NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_biz_no` (`biz_no`),
  KEY `idx_flow_user_time` (`user_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=709 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金流水';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- -------------------------------------------------------------
-- 种子数据：任务分类（含平台佣金比例，单位 %）
-- -------------------------------------------------------------
INSERT INTO `task_category` (`name`, `commission_rate`, `sort`, `status`, `created_at`) VALUES
  ('设计与创意', 10.00, 1, 1, NOW()),
  ('开发与编程', 8.00, 2, 1, NOW()),
  ('文案与翻译', 5.00, 3, 1, NOW())
ON DUPLICATE KEY UPDATE `commission_rate` = VALUES(`commission_rate`);

-- 说明：管理员账号由后端启动时 DataInitializer 自动创建（admin / 123456）
-- 演示账号：alice / 654321（雇主）、bobby / 123456（威客），可用注册接口自行创建
