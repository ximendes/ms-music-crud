package com.example.crud.entrypoint;

import com.example.crud.service.exceptions.MusicAlreadyExistsException;
import com.example.crud.service.exceptions.MusicNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MusicExceptionHandler {

  @ExceptionHandler(MusicAlreadyExistsException.class)
  public ResponseEntity<Void> handleMusicAlreadyExists() {
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }

  @ExceptionHandler(MusicNotFoundException.class)
  public ResponseEntity<Void> handleMusicNotFound() {
    return ResponseEntity.notFound().build();
  }
}
