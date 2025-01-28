package ArticleWebService.handler.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ResponseHandler {

    /**
     * Génère une réponse standardisée pour une API RESTful.
     * <p>
     * Cette méthode encapsule les données, le message, le statut HTTP, et le chemin
     * de la requête dans un objet {@code GenericApiResponse}, et le retourne sous
     * forme d'une {@code ResponseEntity}.
     *
     * @param <T>     Le type des données contenues dans la réponse.
     * @param message Un message décrivant la réponse, destiné à l'utilisateur ou à
     *                l'appelant.
     * @param status  Le statut HTTP de la réponse (par exemple, 200 pour succès, 404
     *                pour non trouvé).
     * @param path    Le chemin de l'endpoint de l'API ayant généré la réponse.
     * @param data    Les données associées à la réponse (peut être null en cas
     *                d'erreur).
     * @return Une {@code ResponseEntity} contenant un objet
     * {@code GenericApiResponse<T>} encapsulant les informations de la
     * réponse.
     * <p>
     * Exemple d'utilisation :
     *
     * <pre>
     * {@code
     * Article article = new Article(1, "Titre de l'article", "Contenu de l'article");
     * ResponseEntity<GenericApiResponse<Article>> response = ResponseHandler.generateResponse(
     *     "Article récupéré avec succès",
     *     HttpStatus.OK,
     *     "/api/articles/1",
     *     article
     * );
     * }
     * </pre>
     * @see ResponseEntity
     * @see GenericApiResponse
     * @see HttpStatus
     */
    public static <T> ResponseEntity<GenericApiResponse<T>> generateResponse(
            String message,
            HttpStatus status,
            String path,
            T data) {

        GenericApiResponse<T> genericApiResponse = new GenericApiResponse<>(
                status.value(),
                message,
                path,
                data
        );
        log.info(genericApiResponse.toString());
        return ResponseEntity.status(status).body(genericApiResponse);
    }
}
