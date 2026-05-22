package com.example.crud.support;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@SpringBootTest
@Testcontainers
public abstract class DynamoDbIntegrationTest {

  private static final String TABLE_NAME = "Music";
  private static final String ARTIST_ATTRIBUTE = "Artist";
  private static final String SONG_TITLE_ATTRIBUTE = "SongTitle";
  private static final String REGION = "us-east-1";
  private static final String ACCESS_KEY = "test";
  private static final String SECRET_KEY = "test";

  /**
   * We manage the container lifecycle manually to avoid Spring context caching issues across
   * integration test classes (a stopped container + cached DynamoDbClient endpoint = connection
   * refused).
   */
  private static final LocalStackContainer LOCALSTACK =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8.1"))
          .withServices("dynamodb");

  @DynamicPropertySource
  static void dynamoDbProperties(DynamicPropertyRegistry registry) {
    LOCALSTACK.start();
    registry.add("aws.dynamodb.endpoint", () -> LOCALSTACK.getEndpoint().toString());
    registry.add("aws.dynamodb.region", LOCALSTACK::getRegion);
    registry.add("aws.dynamodb.access-key", LOCALSTACK::getAccessKey);
    registry.add("aws.dynamodb.secret-key", LOCALSTACK::getSecretKey);
  }

  @BeforeAll
  static void createMusicTable() {
    // Defensive: some JUnit/Spring lifecycles can invoke @BeforeAll before @DynamicPropertySource.
    // Starting is idempotent and makes table creation stable across test classes.
    LOCALSTACK.start();
    try (var client = DynamoDbClient.builder()
                                    .endpointOverride(LOCALSTACK.getEndpoint())
                                    .region(Region.of(REGION))
                                    .credentialsProvider(StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                                    .build()) {
      createTable(client);
      client.waiter().waitUntilTableExists(
          DescribeTableRequest.builder().tableName(TABLE_NAME).build());
    }
  }

  private static void createTable(DynamoDbClient client) {
    try {
      client.createTable(CreateTableRequest.builder()
                                           .tableName(TABLE_NAME)
                                           .attributeDefinitions(
                                               AttributeDefinition.builder()
                                                                  .attributeName(ARTIST_ATTRIBUTE)
                                                                  .attributeType(
                                                                      ScalarAttributeType.S)
                                                                  .build(),
                                               AttributeDefinition.builder()
                                                                  .attributeName(
                                                                      SONG_TITLE_ATTRIBUTE)
                                                                  .attributeType(
                                                                      ScalarAttributeType.S)
                                                                  .build())
                                           .keySchema(
                                               KeySchemaElement.builder()
                                                               .attributeName(ARTIST_ATTRIBUTE)
                                                               .keyType(KeyType.HASH)
                                                               .build(),
                                               KeySchemaElement.builder()
                                                               .attributeName(SONG_TITLE_ATTRIBUTE)
                                                               .keyType(KeyType.RANGE)
                                                               .build())
                                           .billingMode(BillingMode.PAY_PER_REQUEST)
                                           .build());
    } catch (ResourceInUseException ignored) {
      // The container can be shared across subclasses in the same test JVM.
    }
  }
}
