package ArticleWebService.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import ArticleWebService.tools.Message;

public class CustomerResponse {

    private Map<String, Object> detailMessage;

    public CustomerResponse(HttpStatus status,
                            String error,
                            String message,
                            String path) {

        detailMessage = new LinkedHashMap<>();
        detailMessage.put(Message.TIMESTAMP.name(), LocalDateTime.now().toString());
        detailMessage.put(Message.STATUS.name(), status.value());
        detailMessage.put(Message.ERROR.name(), error);
        detailMessage.put(Message.MESSAGE.name(), message);
        detailMessage.put(Message.PATH.name(), path);
    }

    public Map<String, Object> getDetailMessage(){
        return this.detailMessage;
    }

}
