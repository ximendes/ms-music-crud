package com.example.crud.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.dynamodb")
public record DynamoDbProperties(String endpoint,
                                 String region,
                                 String accessKey,
                                 String secretKey) {
}
