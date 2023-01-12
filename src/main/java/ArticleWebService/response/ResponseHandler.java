package ArticleWebService.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ResponseHandler {

    /**
     * Alows to generate an obejct type ResponseEntity whitch contain :
     * a String message, a http status and Object type response.
     *
     * @param message
     * @param status
     * @param responseObject
     * @return an responseEntity with message , status and respoonse object.
     */
    public static ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObject) {


        Map<String, Object> map = new HashMap<>();
        map.put("Message", message);
        map.put("Status", status.value());
        map.put("data", responseObject);

        log.info(map.toString());

        return new ResponseEntity<Object>(map, status);
    }

    /**
     * not implement
     *
     * @param headers
     * @param responseObject
     * @return
     */
    public static ResponseEntity<Object> generateResponse(MultiValueMap<String, String> headers, HttpStatus status) {

        // TODO response headers
        return new ResponseEntity<Object>(headers, status);
    }
}
