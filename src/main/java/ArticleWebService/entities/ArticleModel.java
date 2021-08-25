package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleModel {

    private Long articleId;

    @NotNull(message = "The user id must not be null")
    @NotEmpty(message = "The user id must be present")
    private String userId;


    @NotNull(message = "The Title must not be null")
    @NotEmpty(message = "The Title can not be empty")
    @Size(min = 3, max = 50, message = "The title must be equal or grater than 3 and 50 characters ")
    private String titre;

    @NotNull(message = "The article must not be null")
    @NotEmpty(message = "The article must be empty and must contain the text of article")
    @Size(min = 20, max = 10000, message = "The article must be equal or grater than 3 and 50 characters ")
    private String texte;

/*    @NotNull(message = "The file image must not be null")
    @NotEmpty(message = "The file must contain the image of article")*/
    private MultipartFile fileImage;

/*    @NotNull(message = "A date for article must not be null")
    @NotEmpty(message = "A date for article is obligatory")*/
    private Date date;

}
