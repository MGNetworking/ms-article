package ArticleWebService.handler.security;

import ArticleWebService.handler.response.GenericApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;


@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Permet la gestion des refus sur les end point sécurser
     * @param request
     * @param response
     * @param accessDeniedException
     * @throws IOException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        log.error("CustomAccessDeniedHandler est appelé pour l'URI : {}", request.getRequestURI());

        // Utilisation de ResponseHandler pour générer une réponse structurée
        GenericApiResponse<Object> genericApiResponse = new GenericApiResponse<>(
                HttpStatus.FORBIDDEN.value(),
                "Accès interdit",
                request.getRequestURI(),
                null
        );

        // Configuration de la réponse HTTP
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Sérialisation de l'objet en JSON
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(genericApiResponse));
    }

}