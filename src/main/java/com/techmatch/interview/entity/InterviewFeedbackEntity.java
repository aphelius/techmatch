package com.techmatch.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("interview_feedback")
public class InterviewFeedbackEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("question_id")
    private Long questionId;

    @TableField("user_id")
    private Long userId;

    private Integer score;

    @TableField("feedback_type")
    private String feedbackType;

    private String notes;

    @TableField("confidence_delta")
    private BigDecimal confidenceDelta;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
