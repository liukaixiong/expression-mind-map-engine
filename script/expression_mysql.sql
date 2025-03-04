/*
 Navicat Premium Data Transfer

 Source Server         : TEST
 Source Server Type    : MySQL
 Source Server Version : 80037
 Source Host           : 127.0.0.1:3309
 Source Schema         : xxxx

 Target Server Type    : MySQL
 Target Server Version : 80037
 File Encoding         : 65001

 Date: 25/02/2025 16:08:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for expression_executor_base_info
-- ----------------------------
DROP TABLE IF EXISTS `expression_executor_base_info`;
CREATE TABLE `expression_executor_base_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `service_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '服务名称',
  `business_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '业务编码',
  `executor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '执行器名称',
  `executor_description` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '执行器描述',
  `configurability_json` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '能力配置json',
  `var_definition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变量定义k=v',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '执行器状态:0.创建，1.启用，2.禁用',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除:0否,1.是',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_business_code`(`service_name`, `business_code`, `executor_code`) USING BTREE,
  INDEX `idx_created`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '执行器基本配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for expression_executor_info_config
-- ----------------------------
DROP TABLE IF EXISTS `expression_executor_info_config`;
CREATE TABLE `expression_executor_info_config`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `executor_id` bigint NULL DEFAULT NULL COMMENT '执行器id',
    `parent_id` bigint NULL DEFAULT 0 COMMENT '上级主键编号',
    `expression_type` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'condition' COMMENT '表达式类型:condition 条件表达式;rule 规则表达式',
    `expression_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '表达式编码',
    `expression_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '表达式标题',
    `expression_content` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '表达式内容',
    `expression_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '表达式描述',
    `expression_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '表达式状态:0.禁用,1.启用',
    `configurability_json` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '能力配置json',
    `priority_order` int NULL DEFAULT 100 COMMENT '表达式优先级,数值越高优先级越高',
    `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除:0.否,1.是',
    `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新人',
    `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_executor_id`(`executor_id`, `parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '执行器下面的表达式具体配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for expression_trace_log_index
-- ----------------------------
DROP TABLE IF EXISTS `expression_trace_log_index`;
CREATE TABLE `expression_trace_log_index`  (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `executor_id` bigint NULL DEFAULT NULL COMMENT '执行器编号',
   `service_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务名称',
   `business_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务编码',
   `executor_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器编码',
   `executor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器名称',
   `event_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '事件名称',
   `user_id` bigint NULL DEFAULT NULL COMMENT '用户编号',
   `union_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '唯一编号',
   `trace_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '追踪编号',
   `env_body` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上下文请求体',
   `is_deleted` tinyint NULL DEFAULT 0 COMMENT '状态 1已删除',
   `created` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   `creator` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
   `updated` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
   `updater` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
   PRIMARY KEY (`id`) USING BTREE,
   INDEX `idx_user_id`(`user_id`) USING BTREE,
   INDEX `idx_event_info`(`event_name`, `business_code`, `executor_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for expression_trace_log_info
-- ----------------------------
DROP TABLE IF EXISTS `expression_trace_log_info`;
CREATE TABLE `expression_trace_log_info` (
 `id` bigint NOT NULL AUTO_INCREMENT,
 `trace_log_id` bigint DEFAULT NULL COMMENT '追踪编号主键',
 `executor_id` bigint DEFAULT NULL COMMENT '执行器编号',
 `expression_config_id` bigint DEFAULT NULL COMMENT '表达式配置编号',
 `module_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模块类型: expression:表达式,function:函数',
 `expression_content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表达式内容',
 `expression_result` tinyint DEFAULT NULL COMMENT '表达式结果 0 失败 1成功',
 `debug_trace_content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '调试信息',
 `expression_description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表达式描述',
 `is_deleted` tinyint DEFAULT '0' COMMENT '状态 1已删除',
 `created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `creator` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
 `updated` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `updater` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
 PRIMARY KEY (`id`),
 KEY `idx_trace_log_id` (`trace_log_id`) USING BTREE,
 KEY `idx_config_id_result` (`expression_config_id`,`expression_result`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
