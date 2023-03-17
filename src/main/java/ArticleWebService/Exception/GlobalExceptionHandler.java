package ArticleWebService.Exception;

import ArticleWebService.response.CustomerResponse;
import ArticleWebService.response.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;

/**
 * Gérer les exceptions globale de l'application
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Gestion de l'exception concernant la recherche par ID d'un articles
     *
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(ArticleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleArticleException(ArticleNotFoundException ex,
                                                         WebRequest request,
                                                         HttpServletRequest servletRequest,
                                                         HttpServletResponse httpServletResponse) {

        return ResponseHandler.generateResponse(new CustomerResponse(
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                servletRequest.getRequestURI()));

    }

    /**
     * Centralise les exceptions lors du traitement de validation.
     * Utilisé uniquement pour la validation des formulaires.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {


        Map<String, String> listeError = new HashMap<>();

        // Construction du message a renvoyer a l'utilisateur
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {

            // ignor les messages avec contenant les valeurs null
            if (!error.getDefaultMessage().contains("null")) {
                listeError.put(error.getField(), error.getDefaultMessage());
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDate.now().toString());
        body.put("status", status.value());
        body.put("errors", listeError);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


}
