package com.nodys.botscraper.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.services.youtube.model.*;
import com.nodys.botscraper.dto.youtube.Suggestion;
import com.nodys.botscraper.service.rest.youtube.ApiYouTube;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Component
@Slf4j
public class KafkaConsumer {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String kafkaTopic;
    private final ApiYouTube apiYouTube;
    private final KafkaProducer kafkaProducer;

    public KafkaConsumer(ApiYouTube apiYouTube, KafkaProducer kafkaProducer, @Value(value = "${youtube-scraper-detail-video.topic}") String kafkaTopic) {
        this.apiYouTube = apiYouTube;
        this.kafkaProducer = kafkaProducer;
        this.kafkaTopic = kafkaTopic;
    }

    @KafkaListener(topics = "youtube-scraper")
    public void processMessage(String content) {
        try {
            Suggestion suggestion = mapper.readValue(content, Suggestion.class);
            if(suggestion.getVideoId()!=null){
                VideoListResponse videoDetail = apiYouTube.getVideoDetail(suggestion.getVideoId());
                VideoSnippet snippet = videoDetail.getItems().get(0).getSnippet();
                suggestion.setPublishedAt(snippet.getPublishedAt());
                suggestion.setChannelTitle(snippet.getChannelTitle());
                suggestion.setDescription(snippet.getDescription());
                suggestion.setDefaultLanguage(snippet.getDefaultLanguage());
                suggestion.setChannelTitle(snippet.getChannelTitle());
                VideoContentDetails contentDetails = videoDetail.getItems().get(0).getContentDetails();
                suggestion.setDuration(contentDetails.getDuration());
                VideoStatistics statistics = videoDetail.getItems().get(0).getStatistics();
                suggestion.setCommentCount(statistics.getCommentCount());
                suggestion.setDislikeCount(statistics.getDislikeCount());
                suggestion.setFavoriteCount(statistics.getFavoriteCount());
                suggestion.setLikeCount(statistics.getLikeCount());
                suggestion.setViewCount(statistics.getViewCount());
                CommentThreadListResponse comments = apiYouTube.getVideoComments(suggestion.getIdExp());
                List<CommentThread> listItems = comments.getItems();
                if (comments != null && comments.getItems() != null && comments.getItems().size() > 0) {
                    List<String> listComments = new ArrayList<>();
                    for (CommentThread commentThread :listItems ) {
                        List<Comment> list = commentThread.getReplies().getComments();
                        for (Comment comment :list ) {
                            listComments.add(comment.getSnippet().getTextOriginal());
                        }

                    }
                  //  List<String> listComments = comments.getItems().stream().map(comment -> comment.getSnippet().getTopLevelComment().getSnippet().getTextOriginal()).collect(Collectors.toList());
                    suggestion.setListComments(listComments);
                }
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(suggestion);
                kafkaProducer.send(kafkaTopic, json);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
