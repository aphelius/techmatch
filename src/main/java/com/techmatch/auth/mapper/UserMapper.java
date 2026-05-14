package com.techmatch.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techmatch.auth.entity.AppUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<AppUserEntity> {
}
