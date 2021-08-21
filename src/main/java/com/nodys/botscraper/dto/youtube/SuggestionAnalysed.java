package com.nodys.botscraper.dto.youtube;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.DateTime;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;


@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Data
@Document(indexName = "youtube-scraper-analysed-fix")
public class SuggestionAnalysed implements Serializable {

    @Id
    private String id;

    @Field(name = "@timestamp", type = FieldType.Date)
    private Date timestamp = Date.from(Instant.now());

    private Suggestion suggestion;

    @Field(type = FieldType.Text, fielddata = true)
    private String organ;

    @Field(type = FieldType.Text, fielddata = true)
    private String per;

    @Field(type = FieldType.Text, fielddata = true)
    private String misc;

    @Field(type = FieldType.Text, fielddata = true)
    private String loc;

    @Field(type = FieldType.Text, fielddata = true)
    private String total;

}
