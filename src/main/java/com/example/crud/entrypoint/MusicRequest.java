package com.example.crud.entrypoint;

import com.example.crud.service.MusicDTO;

public record MusicRequest(String artist, String songTitle, Integer year) {

    public MusicDTO toDto() {
        return new MusicDTO(artist, songTitle, year);
    }
}
