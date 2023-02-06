package ArticleWebService.Exception;

public class ArticleNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2028778202108312111L;

    public ArticleNotFoundException(Integer id) {
        super(String.format("L'identifiant %d  de l'article n'a pas était trouver", id));
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
