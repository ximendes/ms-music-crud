package com.example.crud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.crud.repository.Music;
import com.example.crud.repository.MusicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

  private static final String ARTIST = "Daft Punk";
  private static final String SONG_TITLE = "One More Time";
  private static final Integer YEAR = 2000;

  @Mock
  private MusicRepository repository;

  @InjectMocks
  private MusicService service;

  @Test
  void createMusicShouldConvertDtoToEntityAndSave() {
    var dto = new MusicDTO(ARTIST, SONG_TITLE, YEAR);

    service.createMusic(dto);

    var captor = ArgumentCaptor.forClass(Music.class);
    verify(repository, times(1)).save(captor.capture());

    var entity = captor.getValue();
    assertThat(entity.getArtist()).isEqualTo(ARTIST);
    assertThat(entity.getSongTitle()).isEqualTo(SONG_TITLE);
    assertThat(entity.getYear()).isEqualTo(YEAR);
  }

  @Test
  void getMusicShouldLoadEntityAndConvertToDto() {
    var entity = new Music(ARTIST, SONG_TITLE, YEAR);
    when(repository.get(ARTIST, SONG_TITLE)).thenReturn(entity);

    var dto = service.getMusic(ARTIST, SONG_TITLE);

    verify(repository, times(1)).get(ARTIST, SONG_TITLE);
    assertThat(dto.artist()).isEqualTo(ARTIST);
    assertThat(dto.songTitle()).isEqualTo(SONG_TITLE);
    assertThat(dto.year()).isEqualTo(YEAR);
  }

  @Test
  void updateMusicShouldConvertDtoToEntityAndUpdate() {
    var dto = new MusicDTO(ARTIST, SONG_TITLE, YEAR);

    service.updateMusic(dto);

    var captor = ArgumentCaptor.forClass(Music.class);
    verify(repository, times(1)).update(captor.capture());

    var entity = captor.getValue();
    assertThat(entity.getArtist()).isEqualTo(ARTIST);
    assertThat(entity.getSongTitle()).isEqualTo(SONG_TITLE);
    assertThat(entity.getYear()).isEqualTo(YEAR);
  }

  @Test
  void deleteMusicShouldDeleteByArtistAndSong() {
    service.deleteMusic(ARTIST, SONG_TITLE);

    verify(repository, times(1)).delete(ARTIST, SONG_TITLE);
  }
}
