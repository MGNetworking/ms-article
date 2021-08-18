package ArticleWebService.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;


/**
 * @SpringBootTest for integration testing
 */
@SpringBootTest()
@WebMvcTest(controllers = ControllerArticle.class)
public class ControllerArticleTest {

}
