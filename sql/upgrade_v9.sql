-- =============================================================
-- 学习打卡系统 - V9 升级脚本
-- 说明：打卡记录新增"打卡图片"字段 images（JSON 数组存 URL 列表，不限数量）
-- 运行：mysql -uroot -p < upgrade_v9.sql
-- =============================================================

USE study_checkin;

ALTER TABLE `check_in`
  ADD COLUMN `images` TEXT DEFAULT NULL COMMENT '打卡图片URL列表(JSON数组)' AFTER `mood`;
