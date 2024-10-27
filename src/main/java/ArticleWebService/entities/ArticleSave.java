package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Cette classe est utilisé pour la création d'article. Elle possède tous les attributs indispensable pour la
 * création d'un article et la vérification des valeurs attendu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSave {


    /**
     * Doit être null à la création d'un article.
     * Cet attribut est utilisé pour distinguer les anciens article des nouveaux
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

    //@NotNull(message = "l'article doit avoir une image de description")
    //@NotEmpty(message = "Vous devez avoir une images d'en-tête")
    private String imgDescription;

    @NotNull(message = "l'article doit avoir une description")
    @NotEmpty(message = "Vous devez décrire votre article")
    private String description;

    @NotNull(message = "Vous devez faire un choix concernant la visibilité de l'article : true ou false")
    private boolean visibiliter;

    @NotNull(message = "L'article ne doit pas être null")
    @NotEmpty(message = "Vous n'avais pas créer de contenu d'article")
    private String article;

    /**
     * La liste des sources de l'article, si l'article en possède.
     * Cet Attribut n'est pas obligatoire
     */
    private List<String> source;

    /**
     * Permet la vérification l'existence d'un article par la précence de son ID
     *
     * @return Si L'id existe, return true.
     */
    public boolean statusArticle() {

        return this.idArticle != null;
    }
}
