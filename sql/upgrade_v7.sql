-- =============================================================
-- 学习打卡系统 - V7 升级脚本
-- 说明：新增"每日一句"名言表 daily_quote，供首页随机展示
-- 运行：mysql -uroot -p < upgrade_v7.sql
-- =============================================================

USE study_checkin;

CREATE TABLE IF NOT EXISTS `daily_quote` (
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
