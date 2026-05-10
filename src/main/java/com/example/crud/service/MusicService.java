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
    var entity = music.toEntity();
    repository.save(entity);
  }

  public MusicDTO getMusic(String artist, String song) {
    Music music = repository.get(artist, song);
    return music.toDto();
  }

  public void updateMusic(MusicDTO music) {
    var entity = music.toEntity();
    repository.update(entity);
  }

  public void deleteMusic(String artist,  String song) {
    repository.delete(artist, song);
  }
}
