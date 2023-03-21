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
        detailMessage.put(Message.TIMESTAMP.getValues(), LocalDateTime.now().toString());
        detailMessage.put(Message.STATUS.getValues(), status.value());
        detailMessage.put(Message.ERROR.getValues(), error);
        detailMessage.put(Message.MESSAGE.getValues(), message);
        detailMessage.put(Message.PATH.getValues(), path);
    }

    public Map<String, Object> getDetailMessage(){
        return this.detailMessage;
    }

}
