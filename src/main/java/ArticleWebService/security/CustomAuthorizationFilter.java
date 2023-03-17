package ArticleWebService.security;

import ArticleWebService.Exception.CustomerException;
import ArticleWebService.tools.Authentification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.filter.OncePerRequestFilter;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private Authentification authentification;

    public CustomAuthorizationFilter() {
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request
            , HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {


        ObjectMapper objectMapper = new ObjectMapper();
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String uriRequest = httpServletRequest.getRequestURI();
        String headerUser = httpServletRequest.getHeader("user-id");

        log.info("Uri request : " + uriRequest);

        if (uriRequest.equals("/article/saveArticle")) {

            if (headerUser != null) {
                log.info("headerUser : " + headerUser);

                if (!this.authentification.userCreatorArticle(headerUser)) {
                    log.info("n'est authorisé ... ");
                    CustomerException exception = new CustomerException(
                            HttpStatus.FORBIDDEN,
                            "Accès interdit",
                            "Vous n'êtes pas autorisé à accéder à cette ressource.",
                            httpServletRequest.getRequestURI());

                    String jsonMessage = objectMapper.writeValueAsString(exception.getDetailMessage());

                    httpServletResponse.setContentType("application/json");
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    PrintWriter writer = httpServletResponse.getWriter();
                    writer.write(jsonMessage);
                    writer.flush();
                    return;

                }
            }

        }

        log.info("Fait suivre la requête dans le filterChain ");
        filterChain.doFilter(request, response);

    }

}
