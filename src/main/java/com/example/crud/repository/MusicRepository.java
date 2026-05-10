package com.example.crud.repository;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class MusicRepository {

  private final DynamoDbTable<Music> table;

  public MusicRepository(DynamoDbEnhancedClient client) {
    this.table = client.table("Music", TableSchema.fromBean(Music.class));
  }

  public void save(Music entity) {
    table.putItem(entity);
  }

  public void update(Music entity) {
    table.updateItem(entity);
  }

  public Music get(String artist, String song) {
    return table.getItem(Key.builder().partitionValue(artist).sortValue(song).build());
  }

  public void delete(String artist, String song) {
    table.deleteItem(Key.builder().partitionValue(artist). sortValue(song).build());
  }
}
