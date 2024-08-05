package ArticleWebService.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtre des requêtes entrantes
 */
@Slf4j
@Component
public class PreApiFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        log.info("**************");
        log.info("PreApiFilter ");
        log.info("Request ***** ");
        log.info("Path Info :  {}", request.getPathInfo());
        log.info("URI : {}", request.getRequestURI());
        log.info("Headers Access-Control-Allow-Origin : {}", request.getHeaders("Access-Control-Allow-Origin"));
        log.info("Path : {}", request.getPathInfo());
        log.info("Methode : {}", request.getMethod());
        log.info("remote User : {}", request.getRemoteUser());
        log.info("origin- {}", request.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        log.info("Get Headers request - {}", request.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        log.info("**************");

        chain.doFilter(request, response); // Proceed with the next filter in the chain
    }
}
