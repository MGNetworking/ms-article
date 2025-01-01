package ArticleWebService.handler.Exception;

public class InvalidPathVariableException extends RuntimeException {

    private final String pathVariableName;
    private final String raison;

    public InvalidPathVariableException(String pathVariableName, String raison) {
        super(String.format("Valeur invalide pour '%s': %s", pathVariableName, raison));
        this.pathVariableName = pathVariableName;
        this.raison = raison;
    }

    public String getPathVariable() {
        return this.pathVariableName;
    }

    public String getRaison() {
        return this.raison;
    }
}
