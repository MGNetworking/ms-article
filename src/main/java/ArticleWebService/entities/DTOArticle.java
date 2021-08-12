package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOArticle {

    private String userId;

    private String titre;

    private String texte;

    private Date date;

    private String path;

    private String hrefArticle;
}
