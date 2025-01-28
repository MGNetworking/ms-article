package ArticleWebService.handler.Exception;

import ArticleWebService.dto.ErrorDetail;
import ArticleWebService.handler.response.GenericApiResponse;
import ArticleWebService.handler.response.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe responsable de la gestion globale des exceptions dans l'API.
 * Cette classe capture et gère différentes exceptions pour fournir des réponses API structurées et des messages
 * d'erreurs appropriés aux utilisateurs.
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
     * @return Un objet {@link GenericApiResponse <ErrorDetail>} contenant les informations sur l'erreur et le statut HTTP.
     */
    @ExceptionHandler(ArticleException.class)
    public ResponseEntity<GenericApiResponse<ErrorDetail>> handleArticleException(ArticleException ex, WebRequest request) {

        // Récupération de la requête HTTP
        HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();

        // Extraction des en-têtes
        String uri = httpRequest.getRequestURI();
        String contentType = httpRequest.getContentType() != null ? httpRequest.getContentType() : "Unknown";
        String userAgent = httpRequest.getHeader("User-Agent") != null ? httpRequest.getHeader("User-Agent") : "Unknown";

        // Log des informations d'erreur pour le suivi
        log.error("********** [ handleArticleException ] **********");
        log.error("Article Exception détectée !");
        log.error("Message Article      : {}", ex.getArticleMessage());
        log.error("Message error        : {}", ex.getMessage());
        log.error("Statut               : {}", ex.getStatus());
        log.error("URI                  : {}", uri);
        log.error("Content-Type         : {}", contentType);
        log.error("User-Agent           : {}", userAgent);
        log.error("********** [ Fin du Log handleArticleException ] **********");


        return ResponseHandler.generateResponse(
                "Un problème technique est survenu.",
                ex.getStatus(),
                uri,
                new ErrorDetail(
                        ex.getArticleMessage(),
                        "Veuillez contacter l'administrateur système."));
    }


    /**
     * Gestionnaire global pour toutes les exceptions inattendues non spécifiquement traitées.
     * <p>
     * Cette méthode capture les exceptions de type {@link Exception}, enregistre les informations pertinentes
     * dans les logs et retourne une réponse utilisateur générique avec un statut HTTP 500.
     *
     * @param ex      L'exception levée, contenant les informations sur l'erreur survenue.
     * @param request L'objet {@link HttpServletRequest} permettant d'obtenir des informations sur la requête
     *                ayant provoqué l'exception.
     * @return Une réponse standardisée {@link ResponseEntity} contenant :
     * <ul>
     *     <li>Un message utilisateur générique indiquant qu'une erreur technique est survenue.</li>
     *     <li>Un statut HTTP {@code 500 Internal Server Error}.</li>
     *     <li>Le chemin de la requête ayant généré l'exception.</li>
     * </ul>
     * @throws AccessDeniedException Si l'exception capturée est de type {@link AccessDeniedException}.
     *                               Dans ce cas, l'exception est propagée pour être gérée par Spring Security.
     *
     *                               <p>
     *                               <b>Notes:</b>
     *                               <ul>
     *                               <li>
     *                                  Les logs contiennent des informations détaillées sur l'exception, y compris le
     *                                  type d'exception et sa cause, si disponible.
     *                               </li>
     *                               <li>
     *                                  Si une {@link AccessDeniedException} est interceptée, elle est propagée pour
     *                                  une gestion spécifique par un gestionnaire de sécurité personnalisé.
     *                               </li>
     *                               </ul>
     *                               </p>
     *                               <p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericApiResponse<ErrorDetail>> handleGenericException(Exception ex, HttpServletRequest request) throws AccessDeniedException {

        log.error("Une erreur inattendue est survenue : {}, Type : {}", ex.getMessage(), ex.getClass().getName());
        if (ex.getCause() != null) {
            log.error("Cause de l'erreur : {}", ex.getCause().toString());
        } else {
            log.error("Aucune raison détaillée fournie.");
        }


        // Si l'exception est une AccessDeniedException, laissez Spring Security la gérer
        if (ex instanceof AccessDeniedException) {
            log.warn("AccessDeniedException interceptée dans handleGenericException, propagation pour gestion par Spring Security.");
            throw (AccessDeniedException) ex; // Propagation vers CustomAccessDeniedHandler
        }

        return ResponseHandler.generateResponse(
                "Une erreur technique est survenue.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                new ErrorDetail(
                        "Une erreur interne est survenu",
                        "Veuillez contacter l'administrateur système!"));


    }

    /**
     * Gestionnaire global d'exceptions pour les variables de chemin invalides.
     * <p>
     * Cette méthode capture et traite les exceptions de type {@link InvalidPathVariableException},
     * levées lorsqu'une variable de chemin dans une URL est invalide ou non conforme aux attentes.
     *
     * @param ex      L'exception levée, contenant des détails sur la variable de chemin problématique
     *                et la raison de son invalidité.
     * @param request L'objet {@link HttpServletRequest} permettant d'obtenir des informations sur la requête
     *                ayant causé l'exception.
     * @return Une réponse standardisée {@link ResponseEntity}
     */
    @ExceptionHandler(InvalidPathVariableException.class)
    public ResponseEntity<GenericApiResponse<ErrorDetail>> handleInvalidPathVariableException(InvalidPathVariableException ex, HttpServletRequest request) {

        String message = "Une variable de chemin est invalide. Veuillez vérifier l'URL et réessayer.";

        log.error("Erreur de variable de chemin détectée. Message : {}", ex.getMessage());
        if (ex.getRaison() != null) {
            log.error("Raison de l'erreur : {}", ex.getRaison());
        } else {
            log.error("Aucune raison détaillée fournie.");
        }

        return ResponseHandler.generateResponse(
                "URL invalide ou non conforme",
                HttpStatus.NOT_FOUND,
                ex.getPathVariable(),
                new ErrorDetail(
                        ex.getMessage(),
                        "Veuillez contacter l'administrateur système !"));

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
    public ResponseEntity<GenericApiResponse<ErrorDetail>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                "Échec de Validation",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                new ErrorDetail(
                        ex.getMessage(),
                        String.format("La valeur '%s' pour le paramètre '%s' est invalide.", ex.getValue(), ex.getName()))
        );
    }


    /**
     * Gestionnaire global d'exceptions pour les violations de contraintes de validation.
     * <p>
     * Ce gestionnaire capture et traite les exceptions de type {@link ConstraintViolationException},
     * qui surviennent lorsqu'une entité ou des paramètres de requête échouent à valider les contraintes
     * définies (par exemple, via des annotations comme {@code @NotNull}, {@code @Size}, etc.).
     *
     * @param ex      L'exception levée, contenant les détails des violations de contraintes.
     * @param request L'objet {@link HttpServletRequest} permettant d'obtenir des informations sur la requête
     *                ayant provoqué l'exception.
     * @return Une réponse standardisée {@link ResponseEntity} contenant un message utilisateur générique,
     * un code HTTP 400, et une carte des erreurs détaillées associant les champs violés à leurs messages
     * respectifs.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> errorsMap = ex.getConstraintViolations()
                .stream()
                .collect(
                        Collectors.toMap(
                                violation ->
                                        violation.getPropertyPath().toString(), ConstraintViolation::getMessage));


        return ResponseHandler.generateResponse(
                "Échec de Validation",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                errorsMap);

    }


    /**
     * Gestionnaire global d'exceptions pour les erreurs liées à la persistance et à l'intégrité des données.
     * <p>
     * Ce gestionnaire capture et traite les exceptions de type {@link PersistenceException} et
     * {@link DataIntegrityViolationException}, qui peuvent survenir lors des opérations de persistance
     * ou en cas de violation des contraintes de données dans la base.
     *
     * @param ex      L'exception levée, pouvant être une {@link PersistenceException} ou une
     *                {@link DataIntegrityViolationException}.
     * @param request L'objet {@link HttpServletRequest} permettant d'obtenir des informations sur la requête
     *                ayant provoqué l'exception.
     * @return Une réponse standardisée {@link ResponseEntity} contenant un message utilisateur générique,
     * un code HTTP 500, et le chemin de la requête ayant échoué.
     * @throws PersistenceException            Si une erreur technique liée à la persistance des données se produit.
     * @throws DataIntegrityViolationException Si une violation des contraintes d'intégrité des données est détectée.
     */
    @ExceptionHandler({PersistenceException.class, DataIntegrityViolationException.class})
    public ResponseEntity<GenericApiResponse<ErrorDetail>> handlePerDataIntExcep(Exception ex,
                                                                                 HttpServletRequest request) {

        ErrorDetail errorDetail = null;

        if (ex instanceof PersistenceException) {

            errorDetail = new ErrorDetail(
                    "Une erreur technique est survenue lors du traitement de la persistance des données",
                    "Veuillez réessayer plus tard ou contacter l'administrateur système si le problème persiste.");

            log.error("Erreur de persistance détectée. Message : {}", ex.getMessage());
            if (ex.getCause() != null) {
                log.error("Cause de l'erreur : {}", ex.getCause().toString());
            } else {
                log.error("Aucune cause détaillée disponible.");
            }
        }

        if (ex instanceof DataIntegrityViolationException) {
            errorDetail = new ErrorDetail(
                    "Une erreur est survenue lors du traitement des données",
                    "Veuillez vérifier les informations fournies et réessayer.");

            log.error("Violation d'intégrité des données : {}", ex.getMessage());
            if (ex.getCause() != null) {
                log.error("Cause de l'erreur : {}", ex.getCause().toString());
            } else {
                log.error("Aucune cause détaillée disponible.");
            }
        }

        return ResponseHandler.generateResponse(
                "Une erreur technique est survenu",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                errorDetail);
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
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
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
                "Échec de Validation",
                ((ServletWebRequest) request).getRequest().getRequestURI(),
                mapError);


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
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
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
                "Échec de désérialisation",
                ((ServletWebRequest) request).getRequest().getRequestURI(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(genericApiResponse);
    }


}
