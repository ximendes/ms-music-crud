package com.example.crud.service;

import com.example.crud.entrypoint.MusicResponse;
import com.example.crud.repository.Music;

public record MusicDTO(String artist, String songTitle, Integer year) {

    public Music toEntity() {
        return new Music(artist, songTitle, year);
    }

    public MusicResponse toResponse() {
        return new MusicResponse(artist, songTitle, year);
    }
}
