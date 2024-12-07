package ArticleWebService.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleException extends RuntimeException {

    private String message;
    private HttpStatus status;
}
