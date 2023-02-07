package ArticleWebService.Exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerException {

    private Map<String, Object> detailMessage;

    public CustomerException(HttpStatus status, String error, String message, String path) {

        detailMessage = new LinkedHashMap<>();
        detailMessage.put("Timestamp", LocalDateTime.now().toString());
        detailMessage.put("Status", status.value());
        detailMessage.put("Type error", error);
        detailMessage.put("Message", message);
        detailMessage.put("Path", path);
    }

    public Map<String, Object> getDetailMessage(){
        return this.detailMessage;
    }

}
