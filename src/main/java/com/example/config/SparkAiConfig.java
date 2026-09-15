package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.spark")
public class SparkAiConfig {
    private String apiUrl = "https://spark-api-open.xf-yun.com/v1/chat/completions";
    private String apiPassword;
}