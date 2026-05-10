package com.example.crud.support;

import com.example.crud.repository.Music;
import java.util.UUID;

public final class MusicFixtures {

  private MusicFixtures() {
  }

  public static Music music(String songTitle, Integer year) {
    return new Music(uniqueArtist(), songTitle, year);
  }

  public static String uniqueArtist() {
    return "Daft Punk " + UUID.randomUUID();
  }
}
