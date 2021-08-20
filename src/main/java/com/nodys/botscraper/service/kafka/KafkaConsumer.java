package com.nodys.botscraper.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.services.youtube.model.*;
import com.nodys.botscraper.dto.youtube.Suggestion;
import com.nodys.botscraper.dto.youtube.SuggestionAnalysed;
import com.nodys.botscraper.repository.YoutubeScraperRepository;
import com.nodys.botscraper.service.rest.youtube.ApiYouTube;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
    private final YoutubeScraperRepository youtubeScraperRepository;

    public KafkaConsumer(ApiYouTube apiYouTube, KafkaProducer kafkaProducer, @Value(value = "${youtube-scraper-detail-video.topic}") String kafkaTopic, YoutubeScraperRepository youtubeScraperRepository) {
        this.apiYouTube = apiYouTube;
        this.kafkaProducer = kafkaProducer;
        this.kafkaTopic = kafkaTopic;
        this.youtubeScraperRepository = youtubeScraperRepository;
    }

    @KafkaListener(topics = "youtube-scraper")
    public void processMessage(String content) {
        try {
            Suggestion suggestion = mapper.readValue(content, Suggestion.class);

            if(suggestion.getVideoId()!=null){
                VideoListResponse videoDetail = apiYouTube.getVideoDetail(suggestion.getVideoId());
                VideoSnippet snippet = videoDetail.getItems().get(0).getSnippet();
                DateTimeFormatter formater =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                //suggestion.setPublishedAt(LocalDate.parse(snippet.getPublishedAt().toStringRfc3339(), formater));
                Date date = Date.from(LocalDate.parse(snippet.getPublishedAt().toStringRfc3339(), formater).atStartOfDay(ZoneId.systemDefault()).toInstant());
                suggestion.setPublishedAt(date);
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
                try{
                    CommentThreadListResponse comments = apiYouTube.getVideoComments(suggestion.getVideoId());
                    if (comments != null && comments.getItems() != null && comments.getItems().size() > 0) {
                        List<String> listComments = comments.getItems().stream().map(comment -> comment.getSnippet().getTopLevelComment().getSnippet().getTextOriginal()).collect(Collectors.toList());
                        suggestion.setListComments(listComments);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }

                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(suggestion);
                kafkaProducer.send(kafkaTopic, json);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "youtube-scraper-detail-video-analysed")
    public void youtubeAnalyzed(String content) throws JsonProcessingException {
        SuggestionAnalysed youtubeAnalysed = mapper.readValue(content, SuggestionAnalysed.class);
        youtubeScraperRepository.save(youtubeAnalysed);
    }
}
