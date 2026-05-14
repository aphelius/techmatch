package com.techmatch.scoring.scorer;

import com.techmatch.agent.output.DimensionScore;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.resume.dto.StructuredResumeDto;
import com.techmatch.scoring.support.ScoringSupport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ExperienceEducationScorer {

    public DimensionScore score(StructuredResumeDto resumeData, JobStructuredData jobData) {
        int workCount = resumeData.getWorkExperiences() == null ? 0 : resumeData.getWorkExperiences().size();
        int projectCount = resumeData.getProjects() == null ? 0 : resumeData.getProjects().size();
        int educationCount = resumeData.getEducation() == null ? 0 : resumeData.getEducation().size();
        List<String> requirements = jobData.getRequirements() == null ? List.of() : jobData.getRequirements();
        String summary = jobData.getSummary() == null ? "" : jobData.getSummary();
        int extractedYears = ScoringSupport.estimateYearsFromText(summary + " " + String.join(" ", requirements));

        double workSignal = Math.min(1D, workCount / 3.0);
        double projectSignal = Math.min(1D, projectCount / 2.0);
        double educationSignal = educationCount > 0 ? 1D : 0D;
        double yearsSignal = extractedYears <= 0 ? 0.75 : Math.min(1D, workCount / (double) Math.max(1, extractedYears));

        double weighted = workSignal * 0.35 + projectSignal * 0.2 + educationSignal * 0.2 + yearsSignal * 0.25;
        BigDecimal score = BigDecimal.valueOf(weighted)
                .multiply(BigDecimal.valueOf(10))
                .setScale(2, RoundingMode.HALF_UP);

        return DimensionScore.builder()
                .dimension("EXPERIENCE_EDUCATION")
                .score(score)
                .maxScore(BigDecimal.valueOf(10))
                .confidence(BigDecimal.valueOf(Math.max(0.5, Math.min(0.9, 0.55 + weighted * 0.25))))
                .reason("工作经历 %d 段，项目经历 %d 段，教育经历 %d 段".formatted(workCount, projectCount, educationCount))
                .build();
    }
}
