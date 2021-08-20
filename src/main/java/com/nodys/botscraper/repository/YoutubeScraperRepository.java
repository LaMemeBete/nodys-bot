package com.nodys.botscraper.repository;

import com.nodys.botscraper.dto.youtube.SuggestionAnalysed;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface YoutubeScraperRepository extends ElasticsearchRepository<SuggestionAnalysed, String> {

}
