package com.nodys.botscraper.service.rest.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;


@Slf4j
@Service
public class ApiYouTube {

    private static final String DEVELOPER_KEY = "AIzaSyA6pWWp6RAMRmBtDz70J92ZCfWNU7CbpZw";
    private static final String APPLICATION_NAME = "nodys";


    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @param videoId
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     * @throws IOException
     */
    public VideoListResponse getVideoDetail(String videoId) throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        YouTube.Videos.List request = youtubeService.videos()
                .list("snippet,contentDetails,statistics");
        return request.setKey(DEVELOPER_KEY).setId(videoId).execute();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public CommentThreadListResponse getVideoComments(String videoId)
            throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        // Define and execute the API request
        YouTube.CommentThreads.List request = youtubeService.commentThreads()
                .list("snippet,replies");
        return request.setKey(DEVELOPER_KEY)
                .setVideoId("hDkKyAMAobA")
                .execute();
    }
}