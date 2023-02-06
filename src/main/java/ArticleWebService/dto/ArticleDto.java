package ArticleWebService.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {

    private Integer idArticle;
    private Integer idUser;
    private Integer idSection;
    private Integer idCommentaire;
    private Integer idSource;
    private Integer idNote;
    private String titre;
    private String article;
    private String description;

    /**
     * permet la serialisation de l'ojet date au format demander
     */
    @JsonSerialize(using = DateSerialisation.class)
    private Timestamp dateCreation;

    @JsonSerialize(using = DateSerialisation.class)
    private Timestamp dateMaj;
    private int vue;
    private boolean visibiliter;
}
