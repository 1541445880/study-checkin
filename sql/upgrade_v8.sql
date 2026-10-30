-- =============================================================
-- 学习打卡系统 - V8 升级脚本
-- 说明：新增每日学习目标字段 daily_goal（分钟），首页目标进度环用
-- 运行：mysql -uroot -p < upgrade_v8.sql
-- =============================================================

USE study_checkin;

ALTER TABLE `user`
  ADD COLUMN `daily_goal` INT NOT NULL DEFAULT 60 COMMENT '每日学习目标(分钟)' AFTER `gender`;
