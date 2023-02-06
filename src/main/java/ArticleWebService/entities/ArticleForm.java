package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleForm {

    private Integer idUser;

    private Integer idSection;

    // TODO la source doit être référencer ou être gérer pendant l'insertion
    // TODO dans ce une liste doit être reçut
    //private int idSource;

    @NotNull(message = "Le titre ne doit pas être null")
    @NotEmpty(message = "Vous devez mettre un titre")
    @Size(min = 3, max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    private String titre;

    @NotNull(message = "L'article ne doit pas être null")
    @NotEmpty(message = "Vous n'avais pas créer de contenu d'article")
    private String article;

    @NotNull(message = "La visibilité de l'article ne doit pas être null")
    private boolean visibiliter;


}
