package ArticleWebService.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {

    private Long idArticle;
    private Long idUser;
    private Long idSection;
    private Long idCommentaire;
    private Long idSource;
    private Long idNote;
    private String titre;
    private String article;

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
