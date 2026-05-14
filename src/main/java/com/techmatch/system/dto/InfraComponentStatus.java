package com.techmatch.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfraComponentStatus {

    private boolean available;
    private String detail;
}
