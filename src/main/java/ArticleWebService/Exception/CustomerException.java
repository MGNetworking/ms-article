package ArticleWebService.Exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerException {

    private Map<String, Object> detailMessage;

    public CustomerException(HttpStatus status, String error, String message, String path) {

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("timestamp", LocalDateTime.now());
        detail.put("status", status.value());
        detail.put("error", error);
        detail.put("message", message);
        detail.put("path", path);
    }

    public Map<String, Object> getDetailMessage(){
        return this.detailMessage;
    }

}
