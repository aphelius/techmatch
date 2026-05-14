package com.techmatch.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
public class ResumeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_type")
    private String fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("storage_bucket")
    private String storageBucket;

    @TableField("storage_object_key")
    private String storageObjectKey;

    @TableField("storage_url")
    private String storageUrl;

    private String status;

    @TableField("raw_text")
    private String rawText;

    @TableField("structured_json")
    private String structuredJson;

    @TableField("parse_error")
    private String parseError;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
