package ArticleWebService.Exception;

import org.springframework.http.HttpStatus;

public class ArticleException extends RuntimeException {

    private String message;
    private HttpStatus status;

    public ArticleException(){

    }

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

    public void setMessage(String message){
        this.message = message;
    }

    public void setHttpStatus(HttpStatus httpStatus){
        this.status = httpStatus;
    }
}
