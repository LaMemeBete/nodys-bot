package com.nodys.botscraper.dto.youtube;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Data
public class Suggestion implements Serializable {

    private String href;
    private String name;
    private String author;
    private String numberViews;
    private String duration;
    private String since;
    private String text;
    private String idExp;// identifiant unique pour chaque video
    private String iterNumber; // itertation number
    private String seed; //mot cle utlisé


    //une fois clique extraire le titre, le nombre d'abonés, la description , le nombre de vues et la date de publication

    //le nombre de commanirae, le nobre de likes et dislikes
}
