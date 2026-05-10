package com.example.crud.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.crud.service.MusicDTO;
import com.example.crud.service.MusicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class MusicControllerTest {

  private static final String ARTIST = "Daft Punk";
  private static final String SONG_TITLE = "One More Time";
  private static final Integer YEAR = 2000;

  @Mock
  private MusicService service;

  @InjectMocks
  private MusicController controller;

  @Test
  void createMusicShouldReturnCreatedAndSendDtoToService() {
    var request = new MusicRequest(ARTIST, SONG_TITLE, YEAR);

    var response = controller.createMusic(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var captor = ArgumentCaptor.forClass(MusicDTO.class);
    verify(service, times(1)).createMusic(captor.capture());

    var dto = captor.getValue();
    assertThat(dto.artist()).isEqualTo(ARTIST);
    assertThat(dto.songTitle()).isEqualTo(SONG_TITLE);
    assertThat(dto.year()).isEqualTo(YEAR);
  }

  @Test
  void getMusicShouldReturnOkWithMusicResponse() {
    when(service.getMusic(ARTIST, SONG_TITLE))
        .thenReturn(new MusicDTO(ARTIST, SONG_TITLE, YEAR));

    var response = controller.getMusic(ARTIST, SONG_TITLE);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MusicResponse(ARTIST, SONG_TITLE, YEAR));
    verify(service, times(1)).getMusic(ARTIST, SONG_TITLE);
  }

  @Test
  void updateMusicShouldReturnCreatedAndSendDtoToService() {
    var request = new MusicRequest(ARTIST, SONG_TITLE, YEAR);

    var response = controller.updateMusic(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var captor = ArgumentCaptor.forClass(MusicDTO.class);
    verify(service, times(1)).updateMusic(captor.capture());

    var dto = captor.getValue();
    assertThat(dto.artist()).isEqualTo(ARTIST);
    assertThat(dto.songTitle()).isEqualTo(SONG_TITLE);
    assertThat(dto.year()).isEqualTo(YEAR);
  }

  @Test
  void deleteMusicShouldReturnNoContent() {
    var response = controller.deleteMusic(ARTIST, SONG_TITLE);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(service, times(1)).deleteMusic(ARTIST, SONG_TITLE);
  }
}
