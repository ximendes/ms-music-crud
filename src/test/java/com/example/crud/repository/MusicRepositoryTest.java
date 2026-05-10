package com.example.crud.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.same;
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
  void saveShouldPutMusicRecord() {
    var music = new Music(ARTIST, SONG_TITLE, YEAR);

    repository.save(music);

    verify(table, times(1)).putItem(same(music));
  }

  @Test
  void updateShouldPersistMusicRecord() {
    var music = new Music(ARTIST, SONG_TITLE, YEAR);

    repository.update(music);

    verify(table, times(1)).updateItem(same(music));
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
  void deleteShouldDeleteMusicRecordByArtistAndSong() {
    repository.delete(ARTIST, SONG_TITLE);

    var captor = ArgumentCaptor.forClass(Key.class);
    verify(table, times(1)).deleteItem(captor.capture());
    assertThat(captor.getValue().partitionKeyValue().s()).isEqualTo(ARTIST);
    assertThat(captor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo(SONG_TITLE);
  }
}
