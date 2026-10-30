-- =============================================================
-- 学习打卡系统 - V2 升级脚本
-- 说明：给 user 表新增 birthday（出生日期）字段
-- 运行：mysql -uroot -p < upgrade_v2.sql
-- 适用：已通过 schema.sql 建库、不想丢数据的情况
-- =============================================================

USE study_checkin;

ALTER TABLE `user`
  ADD COLUMN `birthday` DATE DEFAULT NULL COMMENT '出生日期' AFTER `avatar`;
