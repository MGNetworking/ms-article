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
     * Permet de générer une reponse formater
     *
     * @param message        le contenu du message
     * @param status         le status code
     * @param responseObject l'objet retourné
     * @return le status http, un message et un objet en format JSON
     */
    public static ResponseEntity<Object> generateResponse(String message,
                                                          HttpStatus status,
                                                          Object responseObject) {

        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.getValues(), message);
        map.put(Message.STATUS.getValues(), status.value());
        map.put(Message.DATA.getValues(), responseObject);
        log.info(map.toString());

        return new ResponseEntity<Object>(map, status);
    }

    /**
     * Permet de générer une reponse formater
     *
     * @param message        le contenu du message
     * @param status         le status code
     * @param responseObject une donnée au format String
     * @return le status http, un message et une données en format String
     * le tout au format JSON
     */
    public static ResponseEntity<Object> generateResponse(String message,
                                                          HttpStatus status,
                                                          String responseObject) {

        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.getValues(), message);
        map.put(Message.STATUS.getValues(), status.value());
        map.put(Message.DATA.getValues(), responseObject);
        log.info(map.toString());

        return new ResponseEntity<Object>(map, status);
    }

    /**
     * Permet de générer une reponse formater
     *
     * @param message        le contenu du message
     * @param status         le status code
     * @param responseObject une donnée au format String
     * @return le status http, un message et une pagination d'article
     * le tout au format JSON
     */
    public static ResponseEntity<Page<Article>> generateResponse(String message,
                                                                 HttpStatus status,
                                                                 Page<Article> responseObject) {
        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.getValues(), message);
        map.put(Message.STATUS.getValues(), status.value());
        map.put(Message.PAGE.getValues(), responseObject);

        log.info(map.toString());

        return ResponseEntity
                .status(status)
                .body((Page<Article>) map);

        // return new ResponseEntity<Page<Article>>((Page<Article>) map, status);
    }


    /**
     * Permet de générer une reponse formater
     *
     * @param message        le contenu du message
     * @param status         le status code
     * @param responseObject un tableau d'objet
     * @return le status http, un message et un tableau d'objet
     * le tout au format JSON
     */
    public static ResponseEntity<Object> generateResponse(String message,
                                                          HttpStatus status,
                                                          Object[] responseObject) {

        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.getValues(), message);
        map.put(Message.STATUS.getValues(), status.value());

        map.put(Message.DATA.getValues(), (String) responseObject[0].toString());
        map.put(Message.VALUE.getValues(), (String) responseObject[1].toString());

        log.info(map.toString());

        return ResponseEntity
                .status(status)
                .body(map);
    }


    /**
     * Permet de générer une reponse formater
     *
     * @param customerResponse le contenu de la réponse
     * @return le status http et le contenu de la réponse dasn un format Customer le tous
     * structuré au format JSON
     */
    public static ResponseEntity<Object> generateResponse(CustomerResponse customerResponse) {

        log.info(customerResponse.getDetailMessage().toString());

        return ResponseEntity
                .status((Integer) customerResponse
                        .getDetailMessage()
                        .get(Message.STATUS.getValues()))
                .body(customerResponse);

    }
}
