package ArticleWebService.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ErrorDetail {
    private String info;
    private String message;

    public ErrorDetail(String info, String message) {
        this.info = info;
        this.message = message;
    }

}
