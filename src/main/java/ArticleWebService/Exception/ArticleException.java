package ArticleWebService.Exception;

public class ArticleException extends RuntimeException {

    private static final long serialVersionUID = -2028778202108312111L;

    public ArticleException(Integer id) {
        super(String.format("L'identifiant de l'article : %d  n'a pas était trouver", id));
    }

    public ArticleException(String message) {
        super(message);
    }

    public ArticleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArticleException(Throwable cause) {
        super(cause);
    }

}
