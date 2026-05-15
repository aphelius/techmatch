package com.techmatch.graph.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evidence_edge")
public class EvidenceEdgeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("from_node_id")
    private Long fromNodeId;

    @TableField("to_node_id")
    private Long toNodeId;

    @TableField("edge_type")
    private String edgeType;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField("create_time")
    private LocalDateTime createTime;
}
