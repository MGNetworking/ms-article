package ArticleWebService.Exception;

import ArticleWebService.response.CustomerResponse;
import ArticleWebService.response.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;

/**
 * Classe responsable de la gestion globale des exceptions dans l'API.
 * Cette classe capture et gère différentes exceptions pour fournir des réponses API structurées et des messages d'erreurs
 * appropriés aux utilisateurs.
 * <p>
 * Utilise {@link ControllerAdvice} pour appliquer les conseils de gestion des exceptions à l'ensemble des contrôleurs.
 * Hérite de {@link ResponseEntityExceptionHandler} pour gérer des exceptions spécifiques comme les erreurs de validation.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Gestionnaire d'exception personnalisé pour les exceptions liées aux articles.
     * <p>
     * Capture l'exception {@link ArticleException} et génère une réponse API à l'utilisateur avec le message d'erreur
     * personnalisé, le statut HTTP et l'URI du point d'accès.
     *
     * @param ex                  L'exception levée par le service d'article.
     * @param request             L'objet {@link WebRequest} représentant la requête en cours.
     * @param servletRequest      L'objet {@link HttpServletRequest} contenant les informations sur la requête HTTP.
     * @param httpServletResponse L'objet {@link HttpServletResponse} contenant les informations sur la réponse HTTP.
     * @return Un objet {@link ResponseEntity} contenant les informations sur l'erreur et le statut HTTP.
     */
    @ExceptionHandler(ArticleException.class)
    public ResponseEntity<Object> handleArticleException(ArticleException ex,
                                                         WebRequest request,
                                                         HttpServletRequest servletRequest,
                                                         HttpServletResponse httpServletResponse) {

        return ResponseHandler.generateResponse(new CustomerResponse(
                ex.getStatus(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                servletRequest.getRequestURI()));

    }

    /**
     * Gère les erreurs de validation d'arguments pour les méthodes annotées avec {@code @Valid}.
     * <p>
     * Cette méthode capture les erreurs de validation des arguments fournis dans les requêtes et génère une réponse
     * structurée avec les champs invalides et les messages d'erreur associés.
     *
     * @param ex      L'exception {@link MethodArgumentNotValidException} levée en cas d'erreurs de validation.
     * @param headers Les en-têtes HTTP à inclure dans la réponse.
     * @param status  Le statut HTTP sélectionné.
     * @param request L'objet {@link WebRequest} représentant la requête en cours.
     * @return Un objet {@link ResponseEntity} contenant les informations d'erreur et le statut HTTP BAD_REQUEST (400).
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        // Construction du message à renvoyer à l'utilisateur pour chaque champ non valide
        Map<String, String> listeError = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            listeError.put(fieldName, errorMessage);
        });

        // Construction du corps de la réponse avec les erreurs et informations associées
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDate.now().toString());
        body.put("status", status.value());
        body.put("errors", listeError);

        // Log des informations d'erreur pour le suivi
        log.error("handleMethodArgumentNotValid **********");
        log.error("timestamp {} ", body.get("timestamp"));
        log.error("status {} ", body.get("status"));
        log.error("errors {} ", body.get("errors"));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les erreurs de désérialisation JSON vers des objets Java lors de la lecture du corps de la requête HTTP.
     * <p>
     * Cette méthode capture les exceptions {@link HttpMessageNotReadableException} levées lorsque le corps de la requête
     * ne peut pas être converti correctement (ex. mauvais format de données).
     *
     * @param ex      L'exception {@link HttpMessageNotReadableException} levée en cas d'erreur de désérialisation.
     * @param headers Les en-têtes HTTP à inclure dans la réponse.
     * @param status  Le statut HTTP sélectionné.
     * @param request L'objet {@link WebRequest} représentant la requête en cours.
     * @return Un objet {@link ResponseEntity} contenant les détails de l'erreur et le statut HTTP BAD_REQUEST (400).
     */
    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        // Message d'erreur détaillé sur la désérialisation
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDate.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erreur de désérialisation");
        body.put("message", ex.getMessage());

        // Log des informations d'erreur pour le suivi
        log.error("handleHttpMessageNotReadable **********");
        log.error("status {} ", body.get("message"));
        log.error("errors {} ", body.get("error"));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


}
