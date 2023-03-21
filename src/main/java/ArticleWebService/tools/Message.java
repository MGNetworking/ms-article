package ArticleWebService.tools;

public enum Message {

    TIMESTAMP("timestamps"),
    STATUS("status"),
    ERROR("error"),
    MESSAGE("message"),
    PATH("path"),
    DATA("data"),
    VALUE("value"),
    PAGE("page");

    private final String valueMessage;

    Message(String value){
        this.valueMessage = value;
    }

    public String getValues(){
        return this.valueMessage;
    }

}
