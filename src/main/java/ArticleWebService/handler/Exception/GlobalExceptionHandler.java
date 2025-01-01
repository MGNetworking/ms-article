package ArticleWebService.handler.Exception;

import ArticleWebService.handler.response.GenericApiResponse;
import ArticleWebService.handler.response.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param ex      L'exception levée par le service d'article.
     * @param request L'objet {@link HttpServletRequest} contenant les informations sur la requête HTTP.
     * @return Un objet {@link GenericApiResponse <Object>} contenant les informations sur l'erreur et le statut HTTP.
     */
    @ExceptionHandler(ArticleException.class)
    public ResponseEntity<GenericApiResponse<Object>> handleArticleException(ArticleException ex,
                                                                             WebRequest request) {

        // Récupération de la requête HTTP
        HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();

        // Extraction des en-têtes
        String uri = httpRequest.getRequestURI();
        String contentType = httpRequest.getContentType() != null ? httpRequest.getContentType().toString() : "Unknown";
        String userAgent = httpRequest.getHeader("User-Agent") != null ? httpRequest.getHeader("User-Agent") : "Unknown";

        // Log des informations d'erreur pour le suivi
        log.error("********** [ handleArticleException ] **********");
        log.error("Article Exception détectée !");
        log.error("Message globale       : {}", ex.getMessage());
        log.error("Message locale        : {}", ex.getMessage());
        log.error("Statut HTTP attendu   : {}", ex.getStatus());
        log.error("URI de la requête     : {}", uri);
        log.error("Content-Type reçu     : {}", contentType);
        log.error("User-Agent            : {}", userAgent);
        log.error("********** [ Fin du Log handleArticleException ] **********");

        return ResponseHandler.generateResponse(
                ex.getArticleMessage(),
                ex.getStatus(),
                uri,
                null
        );
    }


    /**
     * Gestionnaire global pour capturer les exceptions levées
     *
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) throws AccessDeniedException {

        log.error("Une erreur inattendue est survenue : {}, Type : {}", ex.getMessage(), ex.getClass().getName());

        // Si l'exception est une AccessDeniedException, laissez Spring Security la gérer
        if (ex instanceof AccessDeniedException) {
            log.warn("AccessDeniedException interceptée dans handleGenericException, propagation pour gestion par Spring Security.");
            throw (AccessDeniedException) ex; // Propagation vers CustomAccessDeniedHandler
        }

        return ResponseHandler.generateResponse(
                "Une erreur technique est survenue. Veuillez réessayer plus tard.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(InvalidPathVariableException.class)
    public ResponseEntity<GenericApiResponse<Object>> handleGenericInvalidPathVariableException(
            InvalidPathVariableException ex, HttpServletRequest request) {

        log.error("Message globale de l'erreur : {}", ex.getMessage());
        log.error("Message globale plus précis de l'erreur  : {}", ex.getRaison());

        return ResponseHandler.generateResponse(
                ex.getMessage(), // message complet
                HttpStatus.NOT_FOUND,
                ex.getPathVariable(),
                ex.getRaison() // message plus précis
        );
    }


    /**
     * Gère les exceptions causées par un type incompatible pour les arguments d'une méthode,
     * comme lorsqu'une valeur d'un {@code PathVariable} ou d'un {@code RequestParam} ne peut pas être convertie
     * dans le type attendu.
     * <p>
     * Cette méthode traite spécifiquement l'exception {@link MethodArgumentTypeMismatchException}, qui est levée
     * par Spring lorsqu'une valeur fournie dans la requête ne peut pas être convertie dans le type requis.
     * </p>
     *
     * @param ex      l'exception contenant des détails sur l'erreur de conversion, tels que la valeur invalide,
     *                le nom du paramètre et le type requis.
     * @param request la requête HTTP qui a déclenché l'exception, utilisée pour récupérer des informations comme l'URI.
     * @return une {@link ResponseEntity} contenant un objet {@link GenericApiResponse} avec :
     * <ul>
     *     <li>Code de statut : {@link HttpStatus#BAD_REQUEST} (400)</li>
     *     <li>Message : "Validation failed" (Erreur de validation)</li>
     *     <li>Chemin : l'URI de la requête ayant échoué</li>
     *     <li>Données : un message détaillé expliquant quel paramètre est invalide</li>
     * </ul>
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericApiResponse<String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("La valeur '%s' pour le paramètre '%s' est invalide.",
                ex.getValue(), ex.getName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new GenericApiResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        request.getRequestURI(),
                        message
                )
        );
    }


    /**
     * En gestion des erreurs de type @PathVariable, @RequestParam
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericApiResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        new GenericApiResponse<>(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation failed",
                                request.getRequestURI(),
                                errors
                        )
                );
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

        // Récupération des informations utiles
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String contentType = headers.getContentType() != null ? headers.getContentType().toString() : "Unknown";
        String userAgent = headers.getFirst("User-Agent");

        // Construction des erreurs de validation
        Map<String, String> mapError = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            mapError.put(fieldName, errorMessage);
        });

        // Log des informations d'erreur pour le suivi
        log.error("********** [ handleMethodArgumentNotValid ] **********");
        log.error("Erreur de validation !");
        log.error("Liste des erreurs    : {}", mapError);
        log.error("URI                  : {}", uri);
        log.error("Content-Type reçu    : {}", contentType);
        log.error("User-Agent           : {}", userAgent);
        log.error("********** [ handleMethodArgumentNotValid ] **********");

        // Génération de la réponse avec ResponseHandler
        GenericApiResponse<Map<String, String>> genericApiResponse = new GenericApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                ((ServletWebRequest) request).getRequest().getRequestURI(),
                mapError
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(genericApiResponse);

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

        // Récupération des informations utiles
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String contentType = headers.getContentType() != null ? headers.getContentType().toString() : "Unknown";
        String userAgent = headers.getFirst("User-Agent");


        // Log des informations d'erreur pour le suivi
        log.error("********** [ handleHttpMessageNotReadable ] **********");
        log.error("Erreur de désérialisation !");
        log.error("Message d'erreur     : {}", ex.getMessage());
        log.error("URI                  : {}", uri);
        log.error("Content-Type reçu    : {}", contentType);
        log.error("User-Agent           : {}", userAgent);
        log.error("Message d'erreur     : {}", ex.getMessage());
        log.error("********** [ handleHttpMessageNotReadable ] **********");

        // Construction de la réponse avec ApiResponse
        GenericApiResponse<Object> genericApiResponse = new GenericApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur de désérialisation",
                ((ServletWebRequest) request).getRequest().getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(genericApiResponse);
    }


}
