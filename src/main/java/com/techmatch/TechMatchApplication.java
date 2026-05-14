package com.techmatch;

import com.techmatch.config.JwtProperties;
import com.techmatch.config.LlmProperties;
import com.techmatch.config.MinioProperties;
import com.techmatch.config.ResumeProperties;
import com.techmatch.config.WebCorsProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.techmatch.**.mapper")
@ConfigurationPropertiesScan(basePackageClasses = {
        MinioProperties.class,
        JwtProperties.class,
        LlmProperties.class,
        WebCorsProperties.class,
        ResumeProperties.class
})
public class TechMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechMatchApplication.class, args);
    }
}
