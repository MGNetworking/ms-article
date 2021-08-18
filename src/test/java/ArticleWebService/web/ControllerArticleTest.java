package ArticleWebService.web;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @SpringBootTest for integration testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerArticleTest {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void getArticleTest() throws ResponseStatusException, URISyntaxException {

        ResponseEntity<String> result = new ResponseEntity<String>(
                "Error during test execution",
                HttpStatus.INTERNAL_SERVER_ERROR);

        String parameter = "?idArticle=0";
        String basUrl = String.format("http://localhost:8077/ARTICLE-SERVICE/",
                "/getArticle",
                parameter);

        URI uri = new URI(basUrl);

        result = this.restTemplate.getForEntity(uri, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    }


}
