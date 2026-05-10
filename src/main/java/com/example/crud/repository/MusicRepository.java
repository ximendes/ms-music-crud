package com.example.crud.repository;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

@Repository
public class MusicRepository {

  private static final Expression ITEM_DOES_NOT_EXIST =
      Expression.builder()
                .expression("attribute_not_exists(Artist) AND attribute_not_exists(SongTitle)")
                .build();
  private static final Expression ITEM_EXISTS =
      Expression.builder()
                .expression("attribute_exists(Artist) AND attribute_exists(SongTitle)")
                .build();

  private final DynamoDbTable<Music> table;

  public MusicRepository(DynamoDbEnhancedClient client) {
    this.table = client.table("Music", TableSchema.fromBean(Music.class));
  }

  public void save(Music entity) {
    table.putItem(PutItemEnhancedRequest.builder(Music.class)
                                        .item(entity)
                                        .conditionExpression(ITEM_DOES_NOT_EXIST)
                                        .build());
  }

  public void update(Music entity) {
    table.updateItem(UpdateItemEnhancedRequest.builder(Music.class)
                                              .item(entity)
                                              .conditionExpression(ITEM_EXISTS)
                                              .build());
  }

  public Music get(String artist, String song) {
    return table.getItem(Key.builder().partitionValue(artist).sortValue(song).build());
  }

  public boolean exists(String artist, String song) {
    return get(artist, song) != null;
  }

  public void delete(String artist, String song) {
    table.deleteItem(Key.builder().partitionValue(artist).sortValue(song).build());
  }
}
