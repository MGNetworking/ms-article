package ArticleWebService.Exception;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.net.BindException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ServiceExceptionHandler extends ResponseEntityExceptionHandler {


    /**
     * Permet de gérer les erreurs de validation du formulaire.
     *
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDate.now().toString());
        body.put("status", status.value());

/*        List<String> listeError = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .filter(fieldError -> !fieldError.getDefaultMessage().contains("null"))
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.toList());*/

        Map<String, String> listeError = new HashMap<>();

        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            if (!error.getDefaultMessage().contains("null")){
                listeError.put(error.getField(), error.getDefaultMessage());
            }
        }

        body.put("errors", listeError);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


}
