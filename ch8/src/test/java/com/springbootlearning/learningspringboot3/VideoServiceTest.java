package com.springbootlearning.learningspringboot3;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

@ExtendWith(MockitoExtension.class)
public class VideoServiceTest {

  VideoService service;
  @Mock VideoRepository repository;

  @BeforeEach
  void setUp() {
    this.service = new VideoService(repository);
  }

  @Test
  void getVideosShouldReturnAllVideos() {
    // given
    VideoEntity video1 = new VideoEntity("alice", "Spring Boot 3 Intro", "Learn the basics!");
    VideoEntity video2 = new VideoEntity("bob", "Spring Boot 3 Advanced", "Go deep!");
    when(repository.findAll()).thenReturn(List.of(video1, video2));

    // when
    List<VideoEntity> videos = service.getVideos();

    // then
    assertThat(videos).hasSize(2).containsExactly(video1, video2);
    verify(repository).findAll();
  }

  @Test
  void createShouldSaveNewVideo() {
    // given
    VideoEntity expectedVideo = new VideoEntity("alice", "Test Video", "Test Description");
    NewVideo newVideo = new NewVideo("Test Video", "Test Description");
    given(repository.saveAndFlush( any(VideoEntity.class))).willReturn(expectedVideo);

    // when
    VideoEntity result = service.create(newVideo, "alice");

    // then
    assertThat(result.getUsername()).isEqualTo("alice");
    assertThat(result.getName()).isEqualTo("Test Video");
    assertThat(result.getDescription()).isEqualTo("Test Description");
    verify(repository).saveAndFlush( any(VideoEntity.class));
  }

  @Test
  void searchShouldFindVideosByNameOrDescription() {
    // given
    VideoEntity video = new VideoEntity("alice", "Spring Boot 3", "Learn spring basics");
    when(repository.findAll(any(Example.class))).thenReturn(List.of(video));

    // when
    List<VideoEntity> results = service.search(new Search("Spring"));

    // then
    assertThat(results).hasSize(1).contains(video);
    verify(repository).findAll(any(Example.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void searchWithEmptyValueShouldReturnAllVideos() {
    // given
    VideoEntity video1 = new VideoEntity("alice", "Video 1", "Description 1");
    when(repository.findAll(any(Example.class))).thenReturn(List.of(video1));

    // when
    List<VideoEntity> results = service.search(new Search(""));

    // then
    assertThat(results).hasSize(1);
    verify(repository).findAll((Example<VideoEntity>) any(Example.class));
  }

  @Test
  void deleteShouldRemoveExistingVideo() {
    // given
    VideoEntity video = new VideoEntity("alice", "Video to delete", "Description");
    video.setId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(video));

    // when
    service.delete(1L);

    // then
    verify(repository).findById(1L);
    verify(repository).delete(video);
  }

  @Test
  void deleteNonExistentVideoShouldThrowException() {
    // given
    when(repository.findById(999L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.delete(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No video at 999");
  }
}
