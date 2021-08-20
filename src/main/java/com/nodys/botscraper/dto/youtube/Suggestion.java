package com.nodys.botscraper.dto.youtube;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.DateTime;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;


@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Data
public class Suggestion implements Serializable {

    private String href;
    private String idExp;// identifiant unique pour chaque video
    private String videoId;
    private int iterNumber; // itertation number
    private String seed; //mot cle utlisé
    private String channelId;
    private String channelTitle;
    private String defaultAudioLanguage;
    private String defaultLanguage;
    @Field(type = FieldType.Text, fielddata = true)
    private String description;
    private String liveBroadcastContent;
    @Field(type = FieldType.Date)
    private Date publishedAt;
    private String title;
    private String duration;
    private BigInteger commentCount;
    private BigInteger dislikeCount;
    private BigInteger favoriteCount;
    private BigInteger likeCount;
    private BigInteger viewCount;
    @Field(type = FieldType.Text, fielddata = true)
    private List<String> listComments;

}
