-- =============================================================
-- 学习打卡系统 - Docker 数据库自动初始化（第 1 步：建库建表）
-- 说明：mysql 容器首次启动时，/docker-entrypoint-initdb.d 下的 .sql
--       会按文件名字母序自动执行。用 01_ 前缀确保本脚本先于 02_data.sql 执行。
-- =============================================================

CREATE DATABASE IF NOT EXISTS study_checkin
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE study_checkin;

-- -------------------------------------------------------------
-- 用户表
-- -------------------------------------------------------------
CREATE TABLE `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`   VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `birthday`   DATE         DEFAULT NULL COMMENT '出生日期',
  `bg`         VARCHAR(255) DEFAULT NULL COMMENT '个人主页背景图URL',
  `signature`  VARCHAR(100) DEFAULT NULL COMMENT '个性签名',
  `province`   VARCHAR(50)  DEFAULT NULL COMMENT '省份',
  `city`       VARCHAR(50)  DEFAULT NULL COMMENT '城市',
  `district`   VARCHAR(50)  DEFAULT NULL COMMENT '区/县',
  `gender`     VARCHAR(10)  DEFAULT NULL COMMENT '性别(男/女)',
  `daily_goal` INT          NOT NULL DEFAULT 60 COMMENT '每日学习目标(分钟)',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------------------------
-- 打卡记录表
-- -------------------------------------------------------------
CREATE TABLE `check_in` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
  `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
  `check_date`    DATE         NOT NULL COMMENT '打卡日期',
  `study_minutes` INT          NOT NULL DEFAULT 0 COMMENT '学习时长(分钟)',
  `content`       VARCHAR(500) DEFAULT NULL COMMENT '学习内容/心得',
  `mood`          VARCHAR(20)  DEFAULT NULL COMMENT '心情标签',
  `images`        TEXT         DEFAULT NULL COMMENT '打卡图片URL列表(JSON数组)',
  `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习打卡记录表';

-- -------------------------------------------------------------
-- 每日一句名言表
-- -------------------------------------------------------------
CREATE TABLE `daily_quote` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `content`    VARCHAR(200) NOT NULL COMMENT '名言内容',
  `author`     VARCHAR(50)  DEFAULT NULL COMMENT '出处/作者',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日一句名言表';

INSERT INTO `daily_quote` (`content`, `author`) VALUES
('不积跬步，无以至千里；不积小流，无以成江海', '荀子《劝学》'),
('每天进步一点点，坚持带来大改变', NULL),
('种一棵树最好的时间是十年前，其次是现在', '非洲谚语'),
('自律给你自由', NULL),
('你只管努力，剩下的交给时间', NULL),
('今天的努力，是明天的底气', NULL),
('慢慢来，比较快', NULL),
('每一个不曾起舞的日子，都是对生命的辜负', '尼采'),
('宝剑锋从磨砺出，梅花香自苦寒来', '《警世贤文》'),
('业精于勤，荒于嬉；行成于思，毁于随', '韩愈'),
('路漫漫其修远兮，吾将上下而求索', '屈原'),
('纸上得来终觉浅，绝知此事要躬行', '陆游'),
('天行健，君子以自强不息', '《周易》'),
('千里之行，始于足下', '老子'),
('合抱之木，生于毫末；九层之台，起于累土', '老子'),
('锲而不舍，金石可镂', '荀子'),
('书山有路勤为径，学海无涯苦作舟', '韩愈'),
('黑发不知勤学早，白首方悔读书迟', '颜真卿'),
('少壮不努力，老大徒伤悲', '《长歌行》'),
('有志者，事竟成', '《后汉书》');
