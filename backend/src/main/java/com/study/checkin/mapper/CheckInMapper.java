package com.study.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.checkin.entity.CheckIn;
import com.study.checkin.vo.HeatmapVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /**
     * 查询某用户从 startDate 到现在的打卡记录，用于热力图展示
     */
    @Select("SELECT check_date AS date, SUM(study_minutes) AS minutes " +
            "FROM check_in " +
            "WHERE user_id = #{userId} AND check_date >= #{startDate} " +
            "GROUP BY check_date " +
            "ORDER BY check_date")
    List<HeatmapVO> selectHeatmap(@Param("userId") Long userId, @Param("startDate") String startDate);
}
