package ArticleWebService.dto;

import ArticleWebService.entities.Section;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Cette Classe permet de limiter les objets de type Article.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {

    private Integer idArticle;
    private String idUser;
    private Section section ;
    private String titre;
    private String imgUrl;
    private String imgDescription;
    private String description;
    private boolean visibiliter;

    // Serialisation de l'objet date au format => yyyy-MM-dd HH:mm:ss
    @JsonSerialize(using = DateSerialisation.class)
    private Timestamp dateCreation;
    @JsonSerialize(using = DateSerialisation.class)
    private Timestamp dateMaj;


}
