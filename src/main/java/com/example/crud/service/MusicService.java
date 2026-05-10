package com.example.crud.service;

import com.example.crud.repository.Music;
import com.example.crud.repository.MusicRepository;
import org.springframework.stereotype.Service;

@Service
public class MusicService {

  private final MusicRepository repository;

  public MusicService(MusicRepository repository) {
    this.repository = repository;
  }

  public void createMusic(MusicDTO music) {
    validateMusicDoesNotExist(music.artist(), music.songTitle());
    repository.save(music.toEntity());
  }

  public MusicDTO getMusic(String artist, String song) {
    Music music = repository.get(artist, song);
    return music.toDto();
  }

  public void updateMusic(MusicDTO music) {
    validateMusicExists(music.artist(), music.songTitle());
    repository.update(music.toEntity());
  }

  public void deleteMusic(String artist, String song) {
    repository.delete(artist, song);
  }

  private void validateMusicDoesNotExist(String artist, String song) {
    if (repository.exists(artist, song)) {
      throw new MusicAlreadyExistsException();
    }
  }

  private void validateMusicExists(String artist, String song) {
    if (!repository.exists(artist, song)) {
      throw new MusicNotFoundException();
    }
  }
}
