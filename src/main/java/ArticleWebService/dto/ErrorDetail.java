package ArticleWebService.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ErrorDetail {
    private String message;
    private String info;

    public ErrorDetail(String message, String info) {
        this.message = message;
        this.info = info;
    }

}
