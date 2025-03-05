package ArticleWebService.dto;

import ArticleWebService.entities.Section;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleDto {

    private Integer idArticle;
    private Section section;
    private String idUser;

    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères.")
    private String titre;

    @Pattern(regexp = "^https://.*$", message = "L'URL de l'image doit être une URL valide")
    private String imgUrl;
    private String imgDescription;
    private String description;

    private String article;
    private boolean isVisibale;
    private boolean portfolio;

    @Min(value = -1, message = "La valeur de 'vue' doit être au moins -1")
    @Max(value = 1, message = "La valeur de 'vue' ne doit pas dépasser 1")
    private int vue;

    // Serialisation de l'objet date au format => yyyy-MM-dd HH:mm:ss
    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Timestamp dateCreation;

    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Timestamp dateMaj;


}
