package com.jianghu.ling.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianghu.ling.user.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
