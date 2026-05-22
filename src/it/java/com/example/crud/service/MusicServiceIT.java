package com.example.crud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.crud.service.exceptions.MusicAlreadyExistsException;
import com.example.crud.service.exceptions.MusicNotFoundException;
import com.example.crud.support.DynamoDbIntegrationTest;
import com.example.crud.support.MusicDynamoDbTestClient;
import com.example.crud.support.MusicFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

class MusicServiceIT extends DynamoDbIntegrationTest {

  @Autowired
  private MusicService service;

  @Autowired
  private DynamoDbClient dynamoDbClient;

  private MusicDynamoDbTestClient musicDynamoDb;

  @BeforeEach
  void setUp() {
    musicDynamoDb = new MusicDynamoDbTestClient(dynamoDbClient);
  }

  @Test
  void createMusicShouldPersistWhenMusicDoesNotExist() {
    var music = MusicFixtures.music("One More Time", 2000);
    var dto = music.toDto();

    service.createMusic(dto);

    var result = musicDynamoDb.get(dto.artist(), dto.songTitle());
    assertThat(result).isNotNull();
    assertThat(result.getArtist()).isEqualTo(dto.artist());
    assertThat(result.getSongTitle()).isEqualTo(dto.songTitle());
    assertThat(result.getYear()).isEqualTo(dto.year());
  }

  @Test
  void createMusicShouldThrowWhenMusicAlreadyExists() {
    var existing = MusicFixtures.music("Voyager", 2001);
    musicDynamoDb.put(existing);

    var dto = new MusicDTO(existing.getArtist(), existing.getSongTitle(), existing.getYear());

    assertThatThrownBy(() -> service.createMusic(dto))
        .isInstanceOf(MusicAlreadyExistsException.class);

    var result = musicDynamoDb.get(dto.artist(), dto.songTitle());
    assertThat(result).isNotNull();
    assertThat(result.getYear()).isEqualTo(existing.getYear());
  }

  @Test
  void updateMusicShouldThrowWhenMusicDoesNotExist() {
    var music = MusicFixtures.music("Digital Love", 2001);
    var dto = music.toDto();

    assertThatThrownBy(() -> service.updateMusic(dto))
        .isInstanceOf(MusicNotFoundException.class);

    assertThat(musicDynamoDb.get(dto.artist(), dto.songTitle())).isNull();
  }

  @Test
  void updateMusicShouldModifyExistingMusic() {
    var existing = MusicFixtures.music("Around the World", 1997);
    musicDynamoDb.put(existing);

    var updated = new MusicDTO(existing.getArtist(), existing.getSongTitle(), 1998);

    service.updateMusic(updated);

    var result = musicDynamoDb.get(updated.artist(), updated.songTitle());
    assertThat(result).isNotNull();
    assertThat(result.getYear()).isEqualTo(1998);
  }
}

