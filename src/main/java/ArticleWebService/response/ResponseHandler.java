package ArticleWebService.response;

import ArticleWebService.entities.Article;
import ArticleWebService.tools.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ResponseHandler {

    /**
     * Permet de générer une response formater
     *
     * @param message        le contenu du message
     * @param status         le status code
     * @param responseObject l'objet retourné
     * @return le status http, un message et un objet en format JSON
     */
    public static ResponseEntity<Map<String, Object>> generateResponse(String message,
                                                                       HttpStatus status,
                                                                       Object responseObject) {

        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.getValues(), message);
        map.put(Message.STATUS.getValues(), status.value());
        map.put(Message.DATA.getValues(), responseObject);
        log.info(map.toString());

        return ResponseEntity.status(status).body(map);
    }

    /**
     * Permet de générer une response formater
     *
     * @param message        le contenu du message
     * @param status         le status code
     * @param responseObject une donnée au format String
     * @return le status http, un message et une pagination d'article
     * le tout au format JSON
     */
    public static ResponseEntity<Map<String, Object>> generateResponse(String message,
                                                                       HttpStatus status,
                                                                       Page<Article> responseObject) {
        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.getValues(), message);
        map.put(Message.STATUS.getValues(), status.value());
        map.put(Message.PAGE.getValues(), responseObject);

        log.info(map.toString());

        return ResponseEntity.status(status).body(map);

    }

    /**
     * Permet de générer une response formater
     *
     * @param customerResponse le contenu de la réponse
     * @return le status http et le contenu de la réponse dans un format Customer le tout
     * structuré au format JSON
     */
    public static ResponseEntity<Map<String, Object>> generateResponse(CustomerResponse customerResponse) {

        log.info(customerResponse.getDetailMessage().toString());

        return ResponseEntity
                .status((Integer) customerResponse.getDetailMessage().get(Message.STATUS.getValues()))
                .body(customerResponse.getDetailMessage());

    }
}
