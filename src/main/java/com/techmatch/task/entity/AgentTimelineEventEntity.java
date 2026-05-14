package com.techmatch.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_timeline_event")
public class AgentTimelineEventEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("node_name")
    private String nodeName;

    private String status;

    @TableField("input_summary")
    private String inputSummary;

    @TableField("output_summary")
    private String outputSummary;

    @TableField("model_name")
    private String modelName;

    @TableField("prompt_tokens")
    private Integer promptTokens;

    @TableField("completion_tokens")
    private Integer completionTokens;

    @TableField("duration_ms")
    private Long durationMs;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("error_message")
    private String errorMessage;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField("create_time")
    private LocalDateTime createTime;
}
