package com.example.crud.repository;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@ExtendWith(MockitoExtension.class)
class MusicRepositoryTest {

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
  void updateShouldPersistMusicRecord() {
    var music = new Music("Daft Punk", "One More Time", 2000);

    repository.update(music);

    verify(table).updateItem(same(music));
  }
}
