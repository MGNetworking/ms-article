package ArticleWebService.entities;

import ArticleWebService.dto.DateSerialisation;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleForm {

    private Integer idArticle;

    @NotNull(message = "L'identifiant utilisateur doit être présent")
    private Integer idUser;

    @NotNull(message = "La section de l'article doit être présente")
    @Column(name = "id_section")
    private Section section;

    @NotNull(message = "Le titre ne doit pas être null")
    @NotEmpty(message = "Vous devez mettre un titre")
    @Size(min = 3, max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    private String titre;

    //@NotEmpty(message = "Vous devez avoir une images d'enteté")
    private String imgDescription;

    @NotNull(message = "La description ne doit pas être null")
    @NotEmpty(message = "Une description de l'article doit être fournis")
    private String description;

    @NotNull(message = "La visibilité de l'article ne doit pas être null")
    private boolean visibiliter;

    @NotNull(message = "L'article ne doit pas être null")
    @NotEmpty(message = "Vous n'avais pas créer de contenu d'article")
    private String article;

    private List<String> source;
    private Timestamp dateCreation;
    private Timestamp dateMaj;


}
