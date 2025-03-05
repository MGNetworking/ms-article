package ArticleWebService.dto;

import ArticleWebService.entities.Section;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDtoUpdate {

    @NotNull(message = "L'identifiant de l'article est absent ")
    private Integer idArticle;

    @NotNull(message = "L'identifiant utilisateur est absent")
    private String idUser;

    @NotNull(message = "La section de l'article est obligatoire ")
    private Section section;

    @NotBlank(message = "l'article doit avoir un titre ")
    @Size(min = 3, max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    private String titre;

    @NotBlank(message = "l'URL de l'image n'est pas présent")
    @Pattern(regexp = "^https://.*$", message = "L'URL de l'image doit commencer par https://")
    private String imgUrl;

    @NotBlank(message = "La description doit être fourni")
    private String imgDescription;

    @NotBlank(message = "L'article doit avoir une description")
    private String description;

    @NotBlank(message = "L'article doit avoir un contenu")
    private String article;

    private boolean isVisibale ;
    private boolean portfolio;

    @Min(value = -1, message = "La valeur de 'vue' doit être au moins -1")
    @Max(value = 1, message = "La valeur de 'vue' ne doit pas dépasser 1")
    private int vue;

    @NotNull(message = "La date de création est obligatoire")
    @PastOrPresent(message = "La date de création doit être dans le présent ou le passé")
    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Timestamp dateCreation;

    // Variable mise à jour en BD après update
    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Timestamp dateMaj;

}
