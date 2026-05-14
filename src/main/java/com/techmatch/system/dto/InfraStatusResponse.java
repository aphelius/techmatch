package com.techmatch.system.dto;

import lombok.Data;

@Data
public class InfraStatusResponse {

    private InfraComponentStatus database;
    private InfraComponentStatus redis;
    private InfraComponentStatus rabbitmq;
    private InfraComponentStatus minio;
}
