package ArticleWebService.handler.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ResponseHandler {

    /**
     * Permet de générer une response formater
     *
     * @param message le contenu du message
     * @param status  le status code
     * @param data    l'objet retourne
     * @param path    le path URI
     * @return le status http, un message et un objet en format JSON
     */
    public static <T> ResponseEntity<GenericApiResponse<T>> generateResponse(String message,
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
