package com.techmatch.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techmatch.interview.entity.InterviewQuestionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewQuestionMapper extends BaseMapper<InterviewQuestionEntity> {
}
