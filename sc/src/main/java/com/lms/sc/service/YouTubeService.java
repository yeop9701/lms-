package com.lms.sc.service;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

@Service
public class YouTubeService {
	
	@Value("${youtube.api.key}")
	private String apiKey;
	private final String APPLICATION_NAME = "SpringBoot-LMS-youtubeAPI";
	
	public int getVideoDuration(String videoId) {
        try {
            YouTube youtube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null
            )
            .setApplicationName(APPLICATION_NAME)
            .build();

            YouTube.Videos.List request = youtube.videos().list(List.of("contentDetails"));
            request.setKey(apiKey);
            request.setId(List.of(videoId));

            VideoListResponse response = request.execute();
            List<Video> items = response.getItems();

            if (items.isEmpty()) {
                throw new RuntimeException("Video not found");
            }

            Video video = items.get(0);
            String durationStr = video.getContentDetails().getDuration();

            // ISO 8601 duration 형식을 초로 변환
            Duration duration = Duration.parse(durationStr);
            return (int) duration.getSeconds();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching video duration", e);
        }
    }
	
	
	
}
