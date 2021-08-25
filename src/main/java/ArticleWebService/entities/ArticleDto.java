package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleDto implements Serializable {

    private static final long serialVersionUID = 714748166256319694L;

    private Long articleId;

    private String userId;

    private String titre;

    private String texte;

    private MultipartFile fileImage;

    private Date date;

    private String path;

}
