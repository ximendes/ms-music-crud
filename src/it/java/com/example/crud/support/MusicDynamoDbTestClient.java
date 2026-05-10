package com.example.crud.support;

import com.example.crud.repository.Music;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class MusicDynamoDbTestClient {

  private static final String TABLE_NAME = "Music";
  private static final String ARTIST_ATTRIBUTE = "Artist";
  private static final String SONG_TITLE_ATTRIBUTE = "SongTitle";
  private static final String YEAR_ATTRIBUTE = "Year";

  private final DynamoDbClient dynamoDbClient;

  public MusicDynamoDbTestClient(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
  }

  public void put(Music music) {
    dynamoDbClient.putItem(PutItemRequest.builder()
                                         .tableName(TABLE_NAME)
                                         .item(toItem(music))
                                         .build());
  }

  public Music get(String artist, String songTitle) {
    var result = dynamoDbClient.getItem(GetItemRequest.builder()
                                                      .tableName(TABLE_NAME)
                                                      .key(key(artist, songTitle))
                                                      .build());

    if (!result.hasItem()) {
      return null;
    }

    return toMusic(result.item());
  }

  private static Map<String, AttributeValue> toItem(Music music) {
    return Map.of(ARTIST_ATTRIBUTE, AttributeValue.fromS(music.getArtist()),
                  SONG_TITLE_ATTRIBUTE, AttributeValue.fromS(music.getSongTitle()),
                  YEAR_ATTRIBUTE, AttributeValue.fromN(music.getYear().toString()));
  }

  private static Map<String, AttributeValue> key(String artist, String songTitle) {
    return Map.of(ARTIST_ATTRIBUTE, AttributeValue.fromS(artist),
                  SONG_TITLE_ATTRIBUTE, AttributeValue.fromS(songTitle));
  }

  private static Music toMusic(Map<String, AttributeValue> item) {
    return new Music(item.get(ARTIST_ATTRIBUTE).s(),
                     item.get(SONG_TITLE_ATTRIBUTE).s(),
                     Integer.valueOf(item.get(YEAR_ATTRIBUTE).n()));
  }
}
