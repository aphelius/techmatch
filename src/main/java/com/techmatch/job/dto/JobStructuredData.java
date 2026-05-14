package com.techmatch.job.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JobStructuredData {

    private String title;
    private String company;
    private String location;
    private String summary;
    private List<String> requiredSkills = new ArrayList<>();
    private List<String> preferredSkills = new ArrayList<>();
    private List<String> responsibilities = new ArrayList<>();
    private List<String> requirements = new ArrayList<>();
    private List<String> businessKeywords = new ArrayList<>();
}
