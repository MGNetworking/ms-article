package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;

/**
 * Cette classe est utilisé pour la création d'article
 * Elle posséde tous les attributs indispensable pour la
 * création d'un article et la vérification des valeurs attendu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSave {


    /**
     * Doit être null a la création d'un article
     * Cette attribut et utilisé pour distinguer
     * les ancien article des nouveaux
     */
    private Integer idArticle;

    @NotNull(message = "L'identifiant utilisateur est absent")
    private String idUser;

    @NotNull(message = "La section de l'article est obligatoire ")
    private Section section;

    @NotNull(message = "Le titre de l'article est absent")
    @NotEmpty(message = "Vous devez mettre un titre")
    @Size(min = 3, max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    private String titre;

    //@NotNull(message = "l'article doit avoir une images de description")
    //@NotEmpty(message = "Vous devez avoir une images d'en-tête")
    private String imgDescription;

    @NotNull(message = "l'article doit avoir une description")
    @NotEmpty(message = "Vous devez décrire votre article")
    private String description;

    @NotNull(message = "Vous devezz faire un choix concernant la visiblité de l'article : true ou false")
    private boolean visibiliter;

    @NotNull(message = "L'article ne doit pas être null")
    @NotEmpty(message = "Vous n'avais pas créer de contenu d'article")
    private String article;

    private List<String> source;
}
