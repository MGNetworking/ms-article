package ArticleWebService.handler.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Schema(description = "Structure standard pour les réponses de l'API")
public class GenericApiResponse<T> {

    @Schema(description = "Messages détaillés spécifiques à la réponse, comme des erreurs ou des informations additionnelles",
            example = "{\"error\": \"Invalid ID\"}")
    private Map<String, Object> detailMessage;

    @Schema(description = "Horodatage de la réponse", example = "2024-12-13T12:00:00Z")
    private String timestamp;
    @Schema(description = "Statut HTTP de la réponse", example = "200")
    private int status;
    @Schema(description = "Message décrivant la réponse", example = "Liste des articles")
    private String message;
    @Schema(description = "Chemin de l'API qui a généré la réponse", example = "/article/list")
    private String path;
    @Schema(description = "Données de la réponse")
    private T data;

    public GenericApiResponse(int status, String message, String path, T data) {
        this.timestamp = LocalDate.now().toString();
        this.status = status;
        this.message = message;
        this.path = path;
        this.data = data;
    }

}
