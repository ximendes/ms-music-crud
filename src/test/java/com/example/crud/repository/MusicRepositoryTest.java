package com.example.crud.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

@ExtendWith(MockitoExtension.class)
class MusicRepositoryTest {

  private static final String ARTIST = "Daft Punk";
  private static final String SONG_TITLE = "One More Time";
  private static final Integer YEAR = 2000;

  @Mock
  private DynamoDbEnhancedClient client;

  @Mock
  private DynamoDbTable<Music> table;

  private MusicRepository repository;

  @BeforeEach
  void setUp() {
    when(client.table(eq("Music"), org.mockito.ArgumentMatchers.<TableSchema<Music>>any()))
        .thenReturn(table);
    repository = new MusicRepository(client);
  }

  @Test
  @SuppressWarnings("unchecked")
  void saveShouldPutMusicRecord() {
    var music = new Music(ARTIST, SONG_TITLE, YEAR);

    repository.save(music);

    var captor = ArgumentCaptor.forClass(PutItemEnhancedRequest.class);
    verify(table, times(1)).putItem(captor.capture());

    var request = captor.getValue();
    assertThat(request.item()).isSameAs(music);
    assertThat(request.conditionExpression().expression())
        .isEqualTo("attribute_not_exists(Artist) AND attribute_not_exists(SongTitle)");
  }

  @Test
  @SuppressWarnings("unchecked")
  void updateShouldPersistMusicRecord() {
    var music = new Music(ARTIST, SONG_TITLE, YEAR);

    repository.update(music);

    var captor = ArgumentCaptor.forClass(UpdateItemEnhancedRequest.class);
    verify(table, times(1)).updateItem(captor.capture());

    var request = captor.getValue();
    assertThat(request.item()).isSameAs(music);
    assertThat(request.conditionExpression().expression())
        .isEqualTo("attribute_exists(Artist) AND attribute_exists(SongTitle)");
  }

  @Test
  void getShouldLoadMusicRecordByArtistAndSong() {
    var music = new Music(ARTIST, SONG_TITLE, YEAR);
    when(table.getItem(any(Key.class))).thenReturn(music);

    var result = repository.get(ARTIST, SONG_TITLE);

    var captor = ArgumentCaptor.forClass(Key.class);
    verify(table, times(1)).getItem(captor.capture());
    assertThat(captor.getValue().partitionKeyValue().s()).isEqualTo(ARTIST);
    assertThat(captor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo(SONG_TITLE);
    assertThat(result).isSameAs(music);
    assertThat(result.getYear()).isEqualTo(YEAR);
  }

  @Test
  void existsShouldReturnTrueWhenMusicRecordExists() {
    var music = new Music(ARTIST, SONG_TITLE, YEAR);
    when(table.getItem(any(Key.class))).thenReturn(music);

    var result = repository.exists(ARTIST, SONG_TITLE);

    assertThat(result).isTrue();
  }

  @Test
  void existsShouldReturnFalseWhenMusicRecordDoesNotExist() {
    when(table.getItem(any(Key.class))).thenReturn(null);

    var result = repository.exists(ARTIST, SONG_TITLE);

    assertThat(result).isFalse();
  }

  @Test
  void deleteShouldDeleteMusicRecordByArtistAndSong() {
    repository.delete(ARTIST, SONG_TITLE);

    var captor = ArgumentCaptor.forClass(Key.class);
    verify(table, times(1)).deleteItem(captor.capture());
    assertThat(captor.getValue().partitionKeyValue().s()).isEqualTo(ARTIST);
    assertThat(captor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo(SONG_TITLE);
  }
}
