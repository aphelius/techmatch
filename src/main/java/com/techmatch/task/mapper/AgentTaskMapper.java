package com.techmatch.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techmatch.task.entity.AgentTaskEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentTaskMapper extends BaseMapper<AgentTaskEntity> {
}
