-- =============================================================
-- 学习打卡系统 - V3 升级脚本
-- 说明：给 user 表新增 bg（个人主页背景图）字段
-- 运行：mysql -uroot -p < upgrade_v3.sql
-- =============================================================

USE study_checkin;

ALTER TABLE `user`
  ADD COLUMN `bg` VARCHAR(255) DEFAULT NULL COMMENT '个人主页背景图URL' AFTER `birthday`;
