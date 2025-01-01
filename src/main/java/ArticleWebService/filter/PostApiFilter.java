package ArticleWebService.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class PostApiFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        // Proceed with the next filter in the chain
        chain.doFilter(request, response);

        log.info("**************");
        log.info("PostApiFilter ");
        log.info("Request URL - {}", request.getRequestURL());
        log.info("Request Auth Type - {}", request.getAuthType());

        log.info("Response Status - {}", response.getStatus());
        log.info("Response origin - {}", response.getHeader("origin"));
        log.info("Response Header - {}", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        log.info("Response Headers - {}", response.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        log.info("Headers Access-Control-Allow-Origin response - {}", response.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        log.info("**************");

    }
}

