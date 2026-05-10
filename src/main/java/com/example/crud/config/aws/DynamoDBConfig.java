package com.example.crud.config.aws;


import com.example.crud.config.DynamoDbProperties;
import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@EnableConfigurationProperties(DynamoDbProperties.class)
public class DynamoDBConfig {

  @Bean
  public DynamoDbClient dynamoDbClient(DynamoDbProperties props) {
    return DynamoDbClient.builder()
                         .endpointOverride(URI.create(props.endpoint()))
                         .region(Region.of(props.region()))
                         .credentialsProvider(awsCredentials(props))
                         .build();
  }

  private StaticCredentialsProvider awsCredentials(DynamoDbProperties props) {
    return StaticCredentialsProvider.create(
        AwsBasicCredentials.create(props.accessKey(), props.secretKey()));
  }
}
