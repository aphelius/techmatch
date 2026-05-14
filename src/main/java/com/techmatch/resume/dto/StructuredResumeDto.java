package com.techmatch.resume.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StructuredResumeDto {

    private Basics basics = new Basics();
    private List<String> skills = new ArrayList<>();
    private List<Education> education = new ArrayList<>();
    private List<WorkExperience> workExperiences = new ArrayList<>();
    private List<ProjectExperience> projects = new ArrayList<>();
    private List<String> highlights = new ArrayList<>();

    @Data
    public static class Basics {
        private String name;
        private String email;
        private String phone;
        private String summary;
    }

    @Data
    public static class Education {
        private String school;
        private String degree;
        private String major;
        private String period;
    }

    @Data
    public static class WorkExperience {
        private String company;
        private String role;
        private String period;
        private List<String> highlights = new ArrayList<>();
    }

    @Data
    public static class ProjectExperience {
        private String name;
        private String role;
        private String period;
        private List<String> highlights = new ArrayList<>();
    }
}
