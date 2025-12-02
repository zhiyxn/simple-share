/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80015 (8.0.15)
 Source Host           : localhost:3306
 Source Schema         : simple-share

 Target Server Type    : MySQL
 Target Server Version : 80015 (8.0.15)
 File Encoding         : 65001

 Date: 01/12/2025 15:24:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
                            `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                            `category_id` bigint(20) NULL DEFAULT NULL COMMENT '分类ID',
                            `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
                            `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章摘要',
                            `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文章内容',
                            `cover_image` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
                            `author_id` bigint(20) NOT NULL COMMENT '作者ID',
                            `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者姓名',
                            `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下线',
                            `access_level` tinyint(4) NOT NULL DEFAULT 0 COMMENT '访问级别：0-公开 1-会员 2-隐私',
                            `enable_tiered_read` tinyint(4) NOT NULL DEFAULT 1,
                            `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT 'delete flag (0-exist, 2-deleted)',
                            `allow_copy` tinyint(4) NULL DEFAULT 1 COMMENT 'allow copy: 0-no, 1-yes',
                            `preview_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '预览内容',
                            `is_top` tinyint(4) NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
                            `is_recommend` tinyint(4) NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
                            `view_count` bigint(20) NULL DEFAULT 0 COMMENT '浏览次数',
                            `like_count` bigint(20) NULL DEFAULT 0 COMMENT '点赞次数',
                            `collect_count` bigint(20) NULL DEFAULT 0 COMMENT 'collect count',
                            `comment_count` bigint(20) NULL DEFAULT 0 COMMENT '评论次数',
                            `order_num` int(11) NULL DEFAULT 0 COMMENT 'sort order',
                            `share_count` bigint(20) NULL DEFAULT 0 COMMENT '分享次数',
                            `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
                            `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
                            `is_passwd` tinyint(4) NULL DEFAULT NULL COMMENT '是否启动密码，0启用，1不启用',
                            `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章密码',
                            `seo_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO标题',
                            `seo_keywords` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO关键词',
                            `seo_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO描述',
                            `enable_watermark` tinyint(4) NULL DEFAULT 1 COMMENT '水印',
                            `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                            `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                            `member_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会员内容',
                            `review_status` tinyint(4) NULL DEFAULT NULL COMMENT '审核状态（0未审核，1通过，2未通过）',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                            INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
                            INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
                            INDEX `idx_status`(`status` ASC) USING BTREE,
                            INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
                            INDEX `idx_article_access_level`(`access_level` ASC) USING BTREE,
                            FULLTEXT INDEX `ft_title_content`(`title`, `content`)
) ENGINE = InnoDB AUTO_INCREMENT = 88888973 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for article_backup_域名替换
-- ----------------------------
DROP TABLE IF EXISTS `article_backup_域名替换`;
CREATE TABLE `article_backup_域名替换`  (
                                            `id` bigint(20) NOT NULL DEFAULT 0 COMMENT '文章ID',
                                            `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                                            `category_id` bigint(20) NULL DEFAULT NULL COMMENT '分类ID',
                                            `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
                                            `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章摘要',
                                            `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文章内容',
                                            `cover_image` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
                                            `author_id` bigint(20) NOT NULL COMMENT '作者ID',
                                            `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者姓名',
                                            `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下线',
                                            `access_level` tinyint(4) NOT NULL DEFAULT 0 COMMENT '访问级别：0-公开 1-会员 2-隐私',
                                            `enable_tiered_read` tinyint(4) NOT NULL DEFAULT 1,
                                            `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT 'delete flag (0-exist, 2-deleted)',
                                            `allow_copy` tinyint(4) NULL DEFAULT 1 COMMENT 'allow copy: 0-no, 1-yes',
                                            `preview_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '预览内容',
                                            `is_top` tinyint(4) NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
                                            `is_recommend` tinyint(4) NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
                                            `view_count` bigint(20) NULL DEFAULT 0 COMMENT '浏览次数',
                                            `like_count` bigint(20) NULL DEFAULT 0 COMMENT '点赞次数',
                                            `collect_count` bigint(20) NULL DEFAULT 0 COMMENT 'collect count',
                                            `comment_count` bigint(20) NULL DEFAULT 0 COMMENT '评论次数',
                                            `order_num` int(11) NULL DEFAULT 0 COMMENT 'sort order',
                                            `share_count` bigint(20) NULL DEFAULT 0 COMMENT '分享次数',
                                            `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
                                            `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
                                            `is_passwd` tinyint(4) NULL DEFAULT NULL COMMENT '是否启动密码，0启用，1不启用',
                                            `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章密码',
                                            `seo_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO标题',
                                            `seo_keywords` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO关键词',
                                            `seo_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO描述',
                                            `enable_watermark` tinyint(4) NULL DEFAULT 1 COMMENT '水印',
                                            `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                            `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                            `member_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会员内容',
                                            `review_status` tinyint(4) NULL DEFAULT NULL COMMENT '审核状态（0未审核，1通过，2未通过）'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for article_category
-- ----------------------------
DROP TABLE IF EXISTS `article_category`;
CREATE TABLE `article_category`  (
                                     `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                     `tenant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户ID',
                                     `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父分类ID',
                                     `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '祖级列表',
                                     `category_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类路径',
                                     `order_num` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
                                     `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标',
                                     `category_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
                                     `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
                                     `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '分类状态（0正常 1停用）',
                                     `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
                                     `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
                                     `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                     PRIMARY KEY (`category_id`) USING BTREE,
                                     INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                                     INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8889 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for article_copy1
-- ----------------------------
DROP TABLE IF EXISTS `article_copy1`;
CREATE TABLE `article_copy1`  (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
                                  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                                  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '分类ID',
                                  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
                                  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章摘要',
                                  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文章内容',
                                  `cover_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
                                  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
                                  `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者姓名',
                                  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下线',
                                  `access_level` tinyint(4) NOT NULL DEFAULT 0 COMMENT '访问级别：0-公开 1-会员 2-隐私',
                                  `enable_tiered_read` tinyint(4) NOT NULL DEFAULT 1,
                                  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT 'delete flag (0-exist, 2-deleted)',
                                  `allow_copy` tinyint(4) NULL DEFAULT 1 COMMENT 'allow copy: 0-no, 1-yes',
                                  `preview_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '预览内容',
                                  `is_top` tinyint(4) NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
                                  `is_recommend` tinyint(4) NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
                                  `view_count` bigint(20) NULL DEFAULT 0 COMMENT '浏览次数',
                                  `like_count` bigint(20) NULL DEFAULT 0 COMMENT '点赞次数',
                                  `collect_count` bigint(20) NULL DEFAULT 0 COMMENT 'collect count',
                                  `comment_count` bigint(20) NULL DEFAULT 0 COMMENT '评论次数',
                                  `order_num` int(11) NULL DEFAULT 0 COMMENT 'sort order',
                                  `share_count` bigint(20) NULL DEFAULT 0 COMMENT '分享次数',
                                  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
                                  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
                                  `is_passwd` tinyint(4) NULL DEFAULT NULL COMMENT '是否启动密码，0启用，1不启用',
                                  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章密码',
                                  `seo_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO标题',
                                  `seo_keywords` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO关键词',
                                  `seo_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO描述',
                                  `enable_watermark` tinyint(4) NULL DEFAULT 1 COMMENT '水印',
                                  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                  `member_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会员内容',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                                  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
                                  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
                                  INDEX `idx_status`(`status` ASC) USING BTREE,
                                  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
                                  INDEX `idx_article_access_level`(`access_level` ASC) USING BTREE,
                                  FULLTEXT INDEX `ft_title_content`(`title`, `content`)
) ENGINE = InnoDB AUTO_INCREMENT = 14437 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for article_copy2
-- ----------------------------
DROP TABLE IF EXISTS `article_copy2`;
CREATE TABLE `article_copy2`  (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
                                  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                                  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '分类ID',
                                  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
                                  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章摘要',
                                  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文章内容',
                                  `cover_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
                                  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
                                  `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者姓名',
                                  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下线',
                                  `access_level` tinyint(4) NOT NULL DEFAULT 0 COMMENT '访问级别：0-公开 1-会员 2-隐私',
                                  `enable_tiered_read` tinyint(4) NOT NULL DEFAULT 1,
                                  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT 'delete flag (0-exist, 2-deleted)',
                                  `allow_copy` tinyint(4) NULL DEFAULT 1 COMMENT 'allow copy: 0-no, 1-yes',
                                  `preview_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '预览内容',
                                  `is_top` tinyint(4) NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
                                  `is_recommend` tinyint(4) NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
                                  `view_count` bigint(20) NULL DEFAULT 0 COMMENT '浏览次数',
                                  `like_count` bigint(20) NULL DEFAULT 0 COMMENT '点赞次数',
                                  `collect_count` bigint(20) NULL DEFAULT 0 COMMENT 'collect count',
                                  `comment_count` bigint(20) NULL DEFAULT 0 COMMENT '评论次数',
                                  `order_num` int(11) NULL DEFAULT 0 COMMENT 'sort order',
                                  `share_count` bigint(20) NULL DEFAULT 0 COMMENT '分享次数',
                                  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
                                  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
                                  `is_passwd` tinyint(4) NULL DEFAULT NULL COMMENT '是否启动密码，0启用，1不启用',
                                  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章密码',
                                  `seo_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO标题',
                                  `seo_keywords` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO关键词',
                                  `seo_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SEO描述',
                                  `enable_watermark` tinyint(4) NULL DEFAULT 1 COMMENT '水印',
                                  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                  `member_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会员内容',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                                  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
                                  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
                                  INDEX `idx_status`(`status` ASC) USING BTREE,
                                  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
                                  INDEX `idx_article_access_level`(`access_level` ASC) USING BTREE,
                                  FULLTEXT INDEX `ft_title_content`(`title`, `content`)
) ENGINE = InnoDB AUTO_INCREMENT = 14437 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for article_favorite
-- ----------------------------
DROP TABLE IF EXISTS `article_favorite`;
CREATE TABLE `article_favorite`  (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
                                     `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                                     `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                     `article_id` bigint(20) NOT NULL COMMENT '文章ID',
                                     `folder_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '默认收藏夹' COMMENT '收藏夹名称',
                                     `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收藏标签，逗号分隔',
                                     `notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收藏备注',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                     `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for article_tag
-- ----------------------------
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag`  (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
                                `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                                `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
                                `tag_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签颜色',
                                `use_count` bigint(20) NULL DEFAULT 0 COMMENT '使用次数',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_tenant_tag_name`(`tenant_id` ASC, `tag_name` ASC) USING BTREE,
                                INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for article_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `article_tag_relation`;
CREATE TABLE `article_tag_relation`  (
                                         `article_id` bigint(20) NOT NULL COMMENT '文章ID',
                                         `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
                                         PRIMARY KEY (`article_id`, `tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for infra_file
-- ----------------------------
DROP TABLE IF EXISTS `infra_file`;
CREATE TABLE `infra_file`  (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件编号',
                               `config_id` bigint(20) NULL DEFAULT NULL COMMENT '配置编号',
                               `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
                               `path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
                               `url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件 URL',
                               `type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
                               `size` int(11) NOT NULL COMMENT '文件大小',
                               `create_by` bigint(50) NULL DEFAULT NULL COMMENT '创建者',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_by` bigint(50) NULL DEFAULT NULL COMMENT '更新者',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                               `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2166 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for infra_file_config
-- ----------------------------
DROP TABLE IF EXISTS `infra_file_config`;
CREATE TABLE `infra_file_config`  (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
                                      `name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名',
                                      `storage` tinyint(4) NOT NULL COMMENT '存储器',
                                      `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                      `master` bit(1) NOT NULL COMMENT '是否为主配置',
                                      `config` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储配置',
                                      `create_by` bigint(50) NULL DEFAULT NULL COMMENT '创建者',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_by` bigint(50) NULL DEFAULT NULL COMMENT '更新者',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for raw_articles
-- ----------------------------
DROP TABLE IF EXISTS `raw_articles`;
CREATE TABLE `raw_articles`  (
                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                 `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                 `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                 `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                 `cover_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                 `view_count` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                 `like_count` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                 `publish_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                 `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                 `file_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                 `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7114 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
                               `config_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
                               `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名称',
                               `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数键名',
                               `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数键值',
                               `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
                               `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建者',
                               `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新者',
                               `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                               PRIMARY KEY (`config_id`) USING BTREE,
                               UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_email_verification
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_verification`;
CREATE TABLE `sys_email_verification`  (
                                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                                           `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
                                           `scene` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证场景',
                                           `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码',
                                           `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-通过，2-失效',
                                           `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求唯一ID',
                                           `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求IP',
                                           `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户信息',
                                           `expire_time` datetime NOT NULL COMMENT '失效时间',
                                           `verified_time` datetime NULL DEFAULT NULL COMMENT '验证时间',
                                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                           `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                           `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                           PRIMARY KEY (`id`) USING BTREE,
                                           UNIQUE INDEX `uk_request_id`(`request_id` ASC) USING BTREE,
                                           INDEX `idx_tenant_email_scene`(`tenant_id` ASC, `email` ASC, `scene` ASC) USING BTREE,
                                           INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '邮箱验证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
                             `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                             `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
                             `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
                             `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单类型：M-目录，C-菜单，F-按钮',
                             `menu_sort` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
                             `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由地址',
                             `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
                             `query_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由参数',
                             `is_frame` tinyint(4) NULL DEFAULT 1 COMMENT '是否为外链：0-是，1-否',
                             `is_cache` tinyint(4) NULL DEFAULT 0 COMMENT '是否缓存：0-缓存，1-不缓存',
                             `visible` tinyint(4) NOT NULL DEFAULT 1 COMMENT '显示状态：0-隐藏，1-显示',
                             `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '菜单状态：0-禁用，1-启用',
                             `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
                             `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单图标',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                             `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                             INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `tenant_id` bigint(20) NULL DEFAULT NULL COMMENT '租户ID',
                                      `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
                                      `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作标题',
                                      `business_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务类型',
                                      `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '调用类名称',
                                      `method_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '调用方法名称',
                                      `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方式',
                                      `request_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求URI',
                                      `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
                                      `request_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求体',
                                      `response_status` int(11) NULL DEFAULT NULL COMMENT '响应状态码',
                                      `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '执行状态：1成功 0失败',
                                      `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
                                      `operator_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人ID',
                                      `operator_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人名称',
                                      `operator_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人类型',
                                      `client_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端IP',
                                      `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'User-Agent',
                                      `duration` bigint(20) NULL DEFAULT NULL COMMENT '耗时(毫秒)',
                                      `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求ID',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                      `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                      `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `idx_tenant_time`(`tenant_id` ASC, `create_time` ASC) USING BTREE,
                                      INDEX `idx_request_uri`(`request_uri` ASC) USING BTREE,
                                      INDEX `idx_operator`(`operator_id` ASC, `operator_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18039 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                             `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                             `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
                             `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
                             `role_sort` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
                             `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                             `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `uk_tenant_role_code`(`tenant_id` ASC, `role_code` ASC) USING BTREE,
                             INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
                                  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
                                  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_security_settings
-- ----------------------------
DROP TABLE IF EXISTS `sys_security_settings`;
CREATE TABLE `sys_security_settings`  (
                                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `tenant_id` bigint(20) NOT NULL DEFAULT 1 COMMENT '租户ID',
                                          `password_min_length` tinyint(4) NOT NULL DEFAULT 8 COMMENT '密码最小长度',
                                          `password_require_uppercase` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '是否要求大写字母',
                                          `password_require_lowercase` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '是否要求小写字母',
                                          `password_require_number` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '是否要求数字',
                                          `password_require_symbol` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '是否要求特殊字符',
                                          `password_expire_days` smallint(6) NULL DEFAULT NULL COMMENT '密码有效期（天）',
                                          `password_history_count` tinyint(4) NULL DEFAULT 5 COMMENT '密码历史记录数量',
                                          `login_max_attempts` tinyint(4) NOT NULL DEFAULT 5 COMMENT '最大登录失败次数',
                                          `lockout_duration_minutes` smallint(6) NOT NULL DEFAULT 30 COMMENT '锁定时长（分钟）',
                                          `auto_unlock_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '是否自动解锁',
                                          `session_timeout_minutes` smallint(6) NOT NULL DEFAULT 60 COMMENT '会话超时时间（分钟）',
                                          `login_captcha_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '登录是否启用验证码',
                                          `remember_me_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '是否允许记住登录',
                                          `two_factor_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '是否开启双因素认证',
                                          `two_factor_methods` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '双因素认证方式（逗号分隔）',
                                          `ip_whitelist` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'IP白名单（换行分隔）',
                                          `ip_blacklist` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'IP黑名单（换行分隔）',
                                          `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                          `create_by` bigint(20) NULL DEFAULT NULL,
                                          `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                          `update_by` bigint(20) NULL DEFAULT NULL,
                                          `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_security_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统安全设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`  (
                               `tenant_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '租户ID',
                               `tenant_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租户编号',
                               `tenant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租户名称',
                               `contact_user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
                               `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
                               `company_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业名称',
                               `license_number` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
                               `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
                               `domain` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '域名',
                               `intro` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业简介',
                               `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                               `package_id` bigint(20) NULL DEFAULT NULL COMMENT '租户套餐编号',
                               `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
                               `account_count` bigint(20) NULL DEFAULT NULL COMMENT '用户数量（-1不限制）',
                               `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '租户状态（0正常 1停用）',
                               `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                               `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建者',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                               `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新者',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                               PRIMARY KEY (`tenant_id`) USING BTREE,
                               UNIQUE INDEX `uk_tenant_code`(`tenant_code` ASC) USING BTREE,
                               INDEX `idx_domain`(`domain` ASC) USING BTREE,
                               INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_tenant_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_config`;
CREATE TABLE `sys_tenant_config`  (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                      `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                                      `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
                                      `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
                                      `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'string' COMMENT '配置类型：string,number,boolean,json',
                                      `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_tenant_config`(`tenant_id` ASC, `config_key` ASC) USING BTREE,
                                      INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                             `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
                             `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
                             `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
                             `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
                             `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
                             `website` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人网站',
                             `github` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Github 链接',
                             `twitter` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Twitter 链接',
                             `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
                             `email_verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证',
                             `two_factor_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用两步验证',
                             `language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'zh-CN' COMMENT '首选语言',
                             `timezone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'Asia/Shanghai' COMMENT '时区',
                             `email_notifications` json NULL COMMENT '邮件通知配置JSON',
                             `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
                             `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
                             `gender` tinyint(4) NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
                             `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                             `article_review_required` tinyint(1) NOT NULL DEFAULT 1 COMMENT '发布文章是否需要审核（1需要 0无需）',
                             `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '2' COMMENT '0普通 1管理员 3会员',
                             `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
                             `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                             `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                             `vip_expire_time` datetime NULL DEFAULT NULL COMMENT '会员过期时间',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `uk_tenant_username`(`tenant_id` ASC, `username` ASC) USING BTREE,
                             INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                             INDEX `idx_email`(`email` ASC) USING BTREE,
                             INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
                                  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for aitech_card_click
-- ----------------------------
DROP TABLE IF EXISTS `aitech_card_click`;
CREATE TABLE `aitech_card_click`  (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `tenant_id` bigint(20) NOT NULL DEFAULT 1 COMMENT '租户ID',
                                      `card_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡片唯一标识',
                                      `card_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '卡片标题',
                                      `card_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前端路径',
                                      `card_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属分类/学科',
                                      `click_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '累计点击次数',
                                      `last_click_time` datetime NULL DEFAULT NULL COMMENT '最近点击时间',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                                      `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                                      `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_tenant_card_key`(`tenant_id` ASC, `card_key` ASC) USING BTREE,
                                      INDEX `idx_click_count`(`click_count` DESC) USING BTREE,
                                      INDEX `idx_last_click_time`(`last_click_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AiTech 卡片点击统计表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
