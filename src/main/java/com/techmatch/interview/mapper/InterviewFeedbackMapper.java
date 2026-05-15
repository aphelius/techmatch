package com.techmatch.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techmatch.interview.entity.InterviewFeedbackEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewFeedbackMapper extends BaseMapper<InterviewFeedbackEntity> {
}
