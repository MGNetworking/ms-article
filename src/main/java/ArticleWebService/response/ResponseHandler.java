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
     * Allows to generate an objet type ResponseEntity which contain :
     * a String message, a http status and Object type response.
     *
     * @param message
     * @param status
     * @param responseObject
     * @return an responseEntity with message , status and response object.
     */
    public static ResponseEntity<Object> generateResponse(String message,
                                                          HttpStatus status,
                                                          Object responseObject) {
        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.name(), message);
        map.put(Message.STATUS.name(), status.value());
        map.put(Message.DATA.name(), (String) responseObject.toString());
        log.info(map.toString());

        return new ResponseEntity<Object>(map, status);
    }


    public static ResponseEntity<Page<Article>> generateResponse(String message,
                                                                 HttpStatus status,
                                                                 Page<Article> responseObject) {
        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.name(), message);
        map.put(Message.STATUS.name(), status.value());
        map.put(Message.PAGE.name(), responseObject);

        log.info(map.toString());

        return ResponseEntity
                .status(status)
                .body((Page<Article>) map);

       // return new ResponseEntity<Page<Article>>((Page<Article>) map, status);
    }


    public static ResponseEntity<Object> generateResponse(String message,
                                                          HttpStatus status,
                                                          Object[] responseObject) {


        Map<String, Object> map = new HashMap<>();
        map.put(Message.MESSAGE.name(), message);
        map.put(Message.STATUS.name(), status.value());

        map.put(Message.DATA.name(), (String) responseObject[0].toString());
        map.put(Message.VALUE.name(), (String) responseObject[1].toString());

        log.info(map.toString());

        return ResponseEntity
                .status(status)
                .body(map);
    }


    /**
     * Génération d'un reponse Custome
     *
     * @param customerResponse
     * @return
     */
    public static ResponseEntity<Object> generateResponse(CustomerResponse customerResponse) {

        log.info(customerResponse.getDetailMessage().toString());

        return ResponseEntity
                .status((Integer) customerResponse.getDetailMessage().get(Message.STATUS.name()))
                .body(customerResponse);

    }
}
