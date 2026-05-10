package com.example.crud.entrypoint;

import com.example.crud.service.MusicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/music")
public class MusicController {

  private final MusicService service;

  public MusicController(MusicService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<Void> createMusic(@RequestBody MusicRequest music) {
    var dto = music.toDto();
    service.createMusic(dto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{artist}/{song}")
  public ResponseEntity<MusicResponse> getMusic(@PathVariable String artist,
      @PathVariable String song) {
    var music = service.getMusic(artist, song);
    return ResponseEntity.ok(music.toResponse());
  }

  @PutMapping
  public ResponseEntity<Void> updateMusic(@RequestBody MusicRequest music) {
    var dto = music.toDto();
    service.updateMusic(dto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{artist}/{song}")
  public ResponseEntity<Void> deleteMusic(@PathVariable String artist, @PathVariable String song) {
    service.deleteMusic(artist, song);
    return ResponseEntity.noContent().build();
  }
}
