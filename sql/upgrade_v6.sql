-- =============================================================
-- 学习打卡系统 - V6 升级脚本
-- 说明：用户资料新增性别 gender
-- 运行：mysql -uroot -p < upgrade_v6.sql
-- =============================================================

USE study_checkin;

ALTER TABLE `user`
  ADD COLUMN `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别(男/女/保密)' AFTER `district`;
