package com.study.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.checkin.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
