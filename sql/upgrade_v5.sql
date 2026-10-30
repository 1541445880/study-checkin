-- =============================================================
-- 学习打卡系统 - V5 升级脚本
-- 说明：用户资料扩展
--       1) 个性签名 signature
--       2) 省/市/区 province / city / district
-- 运行：mysql -uroot -p < upgrade_v5.sql
-- =============================================================

USE study_checkin;

ALTER TABLE `user`
  ADD COLUMN `signature` VARCHAR(100) DEFAULT NULL COMMENT '个性签名' AFTER `bg`,
  ADD COLUMN `province`  VARCHAR(50)  DEFAULT NULL COMMENT '省份' AFTER `signature`,
  ADD COLUMN `city`      VARCHAR(50)  DEFAULT NULL COMMENT '城市' AFTER `province`,
  ADD COLUMN `district`  VARCHAR(50)  DEFAULT NULL COMMENT '区/县' AFTER `city`;
