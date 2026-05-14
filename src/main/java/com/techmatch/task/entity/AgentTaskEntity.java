package com.techmatch.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_task")
public class AgentTaskEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("job_description_id")
    private Long jobDescriptionId;

    @TableField("task_type")
    private String taskType;

    private String status;

    @TableField("current_node")
    private String currentNode;

    @TableField("error_message")
    private String errorMessage;

    @TableField("report_json")
    private String reportJson;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
