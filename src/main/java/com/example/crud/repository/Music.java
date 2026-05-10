package com.example.crud.repository;

import com.example.crud.service.MusicDTO;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Music {

  private String artist;
  private String songTitle;
  private Integer year;

  public Music() {
  }

  public Music(String artist, String songTitle, Integer year) {
    this.artist = artist;
    this.songTitle = songTitle;
    this.year = year;
  }

  @DynamoDbPartitionKey()
  @DynamoDbAttribute("Artist")
  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  @DynamoDbSortKey
  @DynamoDbAttribute("SongTitle")
  public String getSongTitle() {
    return songTitle;
  }

  public void setSongTitle(String songTitle) {
    this.songTitle = songTitle;
  }

  @DynamoDbAttribute("Year")
  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public MusicDTO toDto() {
    return new MusicDTO(artist, songTitle, year);
  }
}
