package com.example.crud.entrypoint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.crud.support.DynamoDbIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class MusicControllerIT extends DynamoDbIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void postMusicShouldCreateRecord() throws Exception {
    var request = uniqueMusic("One More Time", 2000);

    postMusic(request).andExpect(status().isCreated());

    getMusic(request.artist(), request.songTitle())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.artist").value(request.artist()))
        .andExpect(jsonPath("$.songTitle").value(request.songTitle()))
        .andExpect(jsonPath("$.year").value(request.year()));
  }

  @Test
  void postMusicShouldReturnConflictWhenRecordAlreadyExists() throws Exception {
    var request = uniqueMusic("Voyager", 2001);

    postMusic(request).andExpect(status().isCreated());
    postMusic(request).andExpect(status().isConflict());
  }

  @Test
  void getMusicShouldReturnExistingRecord() throws Exception {
    var request = uniqueMusic("Around the World", 1997);
    postMusic(request).andExpect(status().isCreated());

    getMusic(request.artist(), request.songTitle())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.artist").value(request.artist()))
        .andExpect(jsonPath("$.songTitle").value(request.songTitle()))
        .andExpect(jsonPath("$.year").value(request.year()));
  }

  @Test
  void putMusicShouldUpdateExistingRecord() throws Exception {
    var request = uniqueMusic("Digital Love", 2001);
    postMusic(request).andExpect(status().isCreated());

    var updatedRequest = new MusicRequest(request.artist(), request.songTitle(), 2002);
    putMusic(updatedRequest).andExpect(status().isCreated());

    getMusic(request.artist(), request.songTitle())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.artist").value(request.artist()))
        .andExpect(jsonPath("$.songTitle").value(request.songTitle()))
        .andExpect(jsonPath("$.year").value(updatedRequest.year()));
  }

  @Test
  void putMusicShouldReturnNotFoundWhenRecordDoesNotExist() throws Exception {
    var request = uniqueMusic("Superheroes", 2001);

    putMusic(request).andExpect(status().isNotFound());
  }

  @Test
  void deleteMusicShouldRemoveRecord() throws Exception {
    var request = uniqueMusic("Harder Better Faster Stronger", 2001);
    postMusic(request).andExpect(status().isCreated());

    deleteMusic(request.artist(), request.songTitle())
        .andExpect(status().isNoContent());
  }

  private ResultActions postMusic(MusicRequest request) throws Exception {
    return mockMvc.perform(post("/music")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(objectMapper.writeValueAsString(request)));
  }

  private ResultActions putMusic(MusicRequest request) throws Exception {
    return mockMvc.perform(put("/music")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(objectMapper.writeValueAsString(request)));
  }

  private ResultActions getMusic(String artist, String songTitle) throws Exception {
    return mockMvc.perform(get("/music/{artist}/{song}", artist, songTitle));
  }

  private ResultActions deleteMusic(String artist, String songTitle) throws Exception {
    return mockMvc.perform(delete("/music/{artist}/{song}", artist, songTitle));
  }

  private static MusicRequest uniqueMusic(String songTitle, int year) {
    return new MusicRequest("Daft Punk " + UUID.randomUUID(), songTitle, year);
  }
}
