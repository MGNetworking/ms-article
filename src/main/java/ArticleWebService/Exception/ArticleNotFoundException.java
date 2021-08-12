package ArticleWebService.Exception;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long id) {
        super(String.format("Article with id %d not found", id));
    }

    public ArticleNotFoundException(String message) {
        super(message);
    }

    public ArticleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArticleNotFoundException(Throwable cause) {
        super(cause);
    }

}
