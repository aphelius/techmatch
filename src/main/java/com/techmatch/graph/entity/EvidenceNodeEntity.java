package com.techmatch.graph.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evidence_node")
public class EvidenceNodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("user_id")
    private Long userId;

    @TableField("node_type")
    private String nodeType;

    @TableField("node_key")
    private String nodeKey;

    private String title;

    private String content;

    private String status;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField("create_time")
    private LocalDateTime createTime;
}
