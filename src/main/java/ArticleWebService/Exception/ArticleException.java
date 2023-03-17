package ArticleWebService.Exception;

import org.springframework.http.HttpStatus;

public class ArticleException extends RuntimeException {

    private final String message;
    private final HttpStatus status;

    public ArticleException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }
    public HttpStatus getStatus(){
        return this.status;
    }
}
