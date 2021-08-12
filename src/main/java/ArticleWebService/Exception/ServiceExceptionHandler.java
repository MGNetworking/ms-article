package ArticleWebService.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ServiceExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(ArticleNotFoundException.class)
    public final ResponseEntity<Object> handerNotFoundException(ArticleNotFoundException ex, WebRequest request) {

        CustomerException exception = new CustomerException(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                ex.getMessage(),
                request.getContextPath());

        return new ResponseEntity<>(exception, HttpStatus.NOT_FOUND);
    }

}
