package com.example.crud.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.crud.support.DynamoDbIntegrationTest;
import com.example.crud.support.MusicDynamoDbTestClient;
import com.example.crud.support.MusicFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

class MusicRepositoryIT extends DynamoDbIntegrationTest {

  @Autowired
  private MusicRepository repository;

  @Autowired
  private DynamoDbClient dynamoDbClient;

  private MusicDynamoDbTestClient musicDynamoDb;

  @BeforeEach
  void setUp() {
    musicDynamoDb = new MusicDynamoDbTestClient(dynamoDbClient);
  }

  @Test
  void saveShouldPersistMusicRecordInDynamoDb() {
    var music = MusicFixtures.music("One More Time", 2000);

    repository.save(music);

    var result = musicDynamoDb.get(music.getArtist(), music.getSongTitle());
    assertMusic(result, music.getArtist(), music.getSongTitle(), music.getYear());
  }

  @Test
  void saveShouldNotOverwriteExistingMusicRecordInDynamoDb() {
    var music = MusicFixtures.music("Voyager", 2001);
    musicDynamoDb.put(music);

    var overwrittenMusic = new Music(music.getArtist(), music.getSongTitle(), 2002);

    assertThatThrownBy(() -> repository.save(overwrittenMusic))
        .isInstanceOf(ConditionalCheckFailedException.class);

    var result = musicDynamoDb.get(music.getArtist(), music.getSongTitle());
    assertMusic(result, music.getArtist(), music.getSongTitle(), music.getYear());
  }

  @Test
  void getShouldReturnMusicRecordFromDynamoDb() {
    var music = MusicFixtures.music("Around the World", 1997);
    musicDynamoDb.put(music);

    var result = repository.get(music.getArtist(), music.getSongTitle());

    assertMusic(result, music.getArtist(), music.getSongTitle(), music.getYear());
  }

  @Test
  void existsShouldReturnTrueWhenRecordExistsInDynamoDb() {
    var music = MusicFixtures.music("Aerodynamic", 2001);
    musicDynamoDb.put(music);

    var result = repository.exists(music.getArtist(), music.getSongTitle());

    assertThat(result).isTrue();
  }

  @Test
  void existsShouldReturnFalseWhenRecordDoesNotExistInDynamoDb() {
    var music = MusicFixtures.music("Veridis Quo", 2001);

    var result = repository.exists(music.getArtist(), music.getSongTitle());

    assertThat(result).isFalse();
  }

  @Test
  void updateShouldModifyExistingMusicRecordInDynamoDb() {
    var music = MusicFixtures.music("Digital Love", 2001);

    musicDynamoDb.put(music);

    repository.update(new Music(music.getArtist(), music.getSongTitle(), 2002));

    var result = musicDynamoDb.get(music.getArtist(), music.getSongTitle());
    assertMusic(result, music.getArtist(), music.getSongTitle(), 2002);
  }

  @Test
  void updateShouldNotCreateMissingMusicRecordInDynamoDb() {
    var music = MusicFixtures.music("Superheroes", 2001);

    assertThatThrownBy(() -> repository.update(music))
        .isInstanceOf(ConditionalCheckFailedException.class);

    assertThat(musicDynamoDb.get(music.getArtist(), music.getSongTitle())).isNull();
  }

  @Test
  void deleteShouldRemoveMusicRecordFromDynamoDb() {
    var music = MusicFixtures.music("Harder Better Faster Stronger", 2001);
    musicDynamoDb.put(music);

    repository.delete(music.getArtist(), music.getSongTitle());

    assertThat(musicDynamoDb.get(music.getArtist(), music.getSongTitle())).isNull();
  }

  private static void assertMusic(Music result, String artist, String songTitle, Integer year) {
    assertThat(result).isNotNull();
    assertThat(result.getArtist()).isEqualTo(artist);
    assertThat(result.getSongTitle()).isEqualTo(songTitle);
    assertThat(result.getYear()).isEqualTo(year);
  }
}
