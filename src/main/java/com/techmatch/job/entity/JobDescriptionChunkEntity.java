package com.techmatch.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("job_description_chunk")
public class JobDescriptionChunkEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("job_description_id")
    private Long jobDescriptionId;

    @TableField("user_id")
    private Long userId;

    @TableField("chunk_index")
    private Integer chunkIndex;

    @TableField("chunk_text")
    private String chunkText;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField(exist = false)
    private List<Double> embedding;

    @TableField("create_time")
    private LocalDateTime createTime;
}
