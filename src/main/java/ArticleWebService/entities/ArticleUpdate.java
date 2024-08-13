package ArticleWebService.entities;

import ArticleWebService.dto.DateDeserializer;
import ArticleWebService.dto.DateSerialisation;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
 * Cette classe est utilisé pour la mise à jour des articles
 * Elle posséde tous les attributs indispensable pour la mise
 * à jour d'un article avec la gestion des valeurs attendu
 * pour chaqu'un des ses attribut.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdate {

    @NotNull(message = "L'identifiant de l'article est absent ")
    private Integer idArticle;

    @NotNull(message = "L'identifiant utilisateur est absent")
    private String idUser;

    @NotNull(message = "La section de l'article est obligatoire ")
    private Section section;

    @NotNull(message = "Cette article possédé un titre")
    @NotEmpty(message = "l'article doit avoir un titre ")
    @Size(min = 3, max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    private String titre;

    private String imgUrl;

    //@NotEmpty(message = "L'article doit avoir des images d'en-tête ")
    private String imgDescription;

    @NotNull(message = "l'article doit avoir une description")
    @NotEmpty(message = "Vous devez décrire votre article")
    private String description;

    @NotNull(message = "L'article ne doit pas être null")
    @NotEmpty(message = "Vous n'avais pas créer de contenu d'article")
    private String article;

    @NotNull(message = "L'article à déjà été créer et doit donc posséde une date de création")
    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Timestamp dateCreation;

    @NotNull(message = "Vous devezz faire un choix concernant la visiblité de l'article : true ou false")
    private boolean visibiliter;

    public boolean getVisibiliter() {
        return this.visibiliter;
    }

    /**
     * La liste des soruces de l'article, si l'article en posséde.
     * Cette Attribut n'est pas obligatoire
     */
    private List<String> source;


    /**
     * Permet la vérification l'existence d'un article par la précence de son ID
     *
     * @return Si L'id existe , return false.
     */
    public boolean statusArticle() {
        return this.idArticle == null;
    }
}
