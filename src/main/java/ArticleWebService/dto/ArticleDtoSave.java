package ArticleWebService.dto;

import ArticleWebService.entities.Section;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Cette classe est utilisé pour la création d'article. Elle possède tous les attributs indispensable pour la
 * création d'un article et la vérification des valeurs attendu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDtoSave {

    private Integer idArticle;

    @NotNull(message = "La section de l'article est obligatoire ")
    private Section section;

    @NotBlank(message = "L'identifiant utilisateur est absent")
    private String idUser;

    @NotBlank(message = "Vous devez mettre un titre")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères.")
    private String titre;

    @NotBlank(message = "l'URL de l'image n'est pas présent")
    private String imgUrl;

    @NotEmpty(message = "La description doit être fourni")
    private String imgDescription;

    @NotBlank(message = "L'article ne doit avoir un contenu")
    private String description;

    @NotBlank(message = "L'article doit avoir un contenu")
    private String article;

    // Permet de rendre visible ou non un article a sa création
    private boolean isVisibale;

}
