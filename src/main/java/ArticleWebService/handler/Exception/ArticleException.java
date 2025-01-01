package ArticleWebService.handler.Exception;

import org.springframework.http.HttpStatus;

public class ArticleException extends RuntimeException {

    private final String ArticleMessage;
    private final HttpStatus status;

    public ArticleException(String ArticleMessage, HttpStatus status) {
        super(String.format("Message: '%s' status: %s", ArticleMessage, status));
        this.ArticleMessage = ArticleMessage;
        this.status = status;
    }

    public String getArticleMessage() {
        return ArticleMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
