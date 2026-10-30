package com.study.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.checkin.dto.CheckInRequest;
import com.study.checkin.entity.CheckIn;
import com.study.checkin.entity.User;
import com.study.checkin.exception.BusinessException;
import com.study.checkin.mapper.CheckInMapper;
import com.study.checkin.mapper.UserMapper;
import com.study.checkin.vo.MoodStatVO;
import com.study.checkin.vo.StatsVO;
import com.study.checkin.vo.TodayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /** 打卡（支持补签：checkDate 不传默认今天，传过去日期即补签） */
    public void checkIn(Long userId, CheckInRequest req) {
        LocalDate date = LocalDate.now();
        if (req.getCheckDate() != null && !req.getCheckDate().trim().isEmpty()) {
            try {
                date = LocalDate.parse(req.getCheckDate().trim());
            } catch (Exception e) {
                throw new BusinessException("日期格式错误，应为 yyyy-MM-dd");
            }
            if (date.isAfter(LocalDate.now())) {
                throw new BusinessException("不能打卡未来的日期哦");
            }
        }
        // 支持一天多次打卡，不再做"同一天只能打一次"的限制
        CheckIn c = new CheckIn();
        c.setUserId(userId);
        c.setCheckDate(date);
        c.setStudyMinutes(req.getStudyMinutes());
        c.setContent(req.getContent());
        c.setMood(req.getMood());
        c.setImages(encodeImages(req.getImages()));
        checkInMapper.insert(c);
    }

    /** 删除打卡记录（只能删自己的） */
    public void delete(Long userId, Long id) {
        CheckIn c = checkInMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        checkInMapper.deleteById(id);
    }

    /** 查询单条记录（编辑回显，只能看自己的） */
    public CheckIn get(Long userId, Long id) {
        CheckIn c = checkInMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        fillImages(c);
        return c;
    }

    /** 更新打卡记录（只能改自己的；日期允许补签修改，不允许未来） */
    public void update(Long userId, Long id, CheckInRequest req) {
        CheckIn c = checkInMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        LocalDate date = c.getCheckDate();
        if (req.getCheckDate() != null && !req.getCheckDate().trim().isEmpty()) {
            try {
                date = LocalDate.parse(req.getCheckDate().trim());
            } catch (Exception e) {
                throw new BusinessException("日期格式错误，应为 yyyy-MM-dd");
            }
            if (date.isAfter(LocalDate.now())) {
                throw new BusinessException("不能打卡未来的日期哦");
            }
        }
        c.setCheckDate(date);
        c.setStudyMinutes(req.getStudyMinutes());
        c.setContent(req.getContent());
        c.setMood(req.getMood());
        if (req.getImages() != null) {
            c.setImages(encodeImages(req.getImages()));
        }
        checkInMapper.updateById(c);
    }

    /** 今日打卡状态（聚合：支持一天多次） */
    public TodayVO today(Long userId) {
        TodayVO vo = new TodayVO();
        LocalDate now = LocalDate.now();
        List<CheckIn> todayList = checkInMapper.selectList(new QueryWrapper<CheckIn>()
                .eq("user_id", userId).eq("check_date", now));
        vo.setTodayChecked(!todayList.isEmpty());
        vo.setTodayCount((long) todayList.size());
        vo.setTodayMinutes(todayList.stream()
                .mapToInt(c -> c.getStudyMinutes() == null ? 0 : c.getStudyMinutes())
                .sum());
        return vo;
    }

    /** 打卡记录分页列表 */
    public List<CheckIn> list(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<CheckIn> list = checkInMapper.selectList(new QueryWrapper<CheckIn>()
                .eq("user_id", userId)
                .orderByDesc("check_date")
                .last("LIMIT " + offset + ", " + size));
        list.forEach(this::fillImages);
        return list;
    }

    /** 统计总览 */
    public StatsVO stats(Long userId) {
        StatsVO vo = new StatsVO();
        LocalDate today = LocalDate.now();

        // 今日状态（聚合：一天多次打卡则累加）
        List<CheckIn> todayList = checkInMapper.selectList(new QueryWrapper<CheckIn>()
                .eq("user_id", userId).eq("check_date", today));
        vo.setTodayChecked(!todayList.isEmpty());
        vo.setTodayMinutes(todayList.stream()
                .mapToInt(c -> c.getStudyMinutes() == null ? 0 : c.getStudyMinutes())
                .sum());

        // 每日学习目标（未设置默认 60 分钟）
        User user = userMapper.selectById(userId);
        int goal = (user != null && user.getDailyGoal() != null) ? user.getDailyGoal() : 60;
        vo.setDailyGoal(goal);

        // 全部打卡记录
        List<CheckIn> all = checkInMapper.selectList(new QueryWrapper<CheckIn>().eq("user_id", userId));
        // 累计天数 = 去重后的打卡日期数（一天多次只算一天）
        long distinctDays = all.stream()
                .map(CheckIn::getCheckDate)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
        vo.setTotalDays(distinctDays);
        vo.setTotalMinutes(all.stream()
                .mapToLong(c -> c.getStudyMinutes() == null ? 0 : c.getStudyMinutes())
                .sum());

        // 连续打卡天数
        vo.setStreak(calcStreak(all));

        // 近一年热力图数据
        String startDate = today.minusYears(1).toString();
        vo.setHeatmap(checkInMapper.selectHeatmap(userId, startDate));

        // 心情分布（饼图数据，按次数降序）
        Map<String, Long> moodCount = all.stream()
                .filter(c -> c.getMood() != null && !c.getMood().trim().isEmpty())
                .collect(Collectors.groupingBy(CheckIn::getMood, Collectors.counting()));
        List<MoodStatVO> moodStats = moodCount.entrySet().stream()
                .map(e -> new MoodStatVO(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparingInt(MoodStatVO::getCount).reversed())
                .collect(Collectors.toList());
        vo.setMoodStats(moodStats);

        return vo;
    }

    /** 图片URL列表 → JSON 字符串（空则返回 null） */
    private String encodeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            throw new BusinessException("图片数据处理失败");
        }
    }

    /** JSON 字符串 → 图片URL列表（填充 imageList 字段） */
    private void fillImages(CheckIn c) {
        if (c == null) {
            return;
        }
        if (c.getImages() == null || c.getImages().isEmpty()) {
            c.setImageList(new ArrayList<>());
            return;
        }
        try {
            c.setImageList(objectMapper.readValue(c.getImages(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            c.setImageList(new ArrayList<>());
        }
    }

    /**
     * 计算连续打卡天数：
     * 从今天开始往前数；若今天没打，则从昨天开始数，直到遇到断签。
     */
    private Integer calcStreak(List<CheckIn> list) {
        Set<LocalDate> dates = list.stream().map(CheckIn::getCheckDate).collect(Collectors.toSet());
        LocalDate cur = LocalDate.now();
        if (!dates.contains(cur)) {
            cur = cur.minusDays(1);
        }
        int streak = 0;
        while (dates.contains(cur)) {
            streak++;
            cur = cur.minusDays(1);
        }
        return streak;
    }
}
