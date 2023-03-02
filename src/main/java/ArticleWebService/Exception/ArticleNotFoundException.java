package ArticleWebService.Exception;

public class ArticleNotFoundException extends RuntimeException {

    private final String message;

    public ArticleNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }


}
