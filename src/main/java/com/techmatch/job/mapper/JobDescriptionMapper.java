package com.techmatch.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techmatch.job.entity.JobDescriptionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobDescriptionMapper extends BaseMapper<JobDescriptionEntity> {
}
