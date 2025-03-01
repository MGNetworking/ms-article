package ArticleWebService.integration;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.projection.ArticleProjection;
import ArticleWebService.service.ArticleServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(ArticleServiceImpl.class)
@Transactional
public class ArticleServiceITTest {

    @Autowired
    private ArticleServiceImpl articleService;

    @MockBean
    private ModelMapper modelMapper;

    @Test
    @DisplayName("findArticlesPagination devrait retourner une page d'articles complètement mappés avec leurs sections")
    void shouldReturnPageOfCorrectlyMappedArticles_whenFindArticlesPaginationCalled() throws JsonProcessingException {

        // Arrange
        int page = 0;
        int size = 10;

        // Act
        Page<ArticleDto> result = articleService.findArticlesPagination(page, size);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(result);
        System.out.println("JSON qui serait envoyé par l'API : " + json);

        // Assert
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertFalse(result.isEmpty(), "L'objet de pagination ne doit pas être vide");

        assertNotNull(result.getContent().get(0).getSection(),
                "L'objet doit Section doit être récupérer par Hibernate.initialize()");

        assertEquals("Java", result.getContent().get(0).getSection().getDescription(),
                "La description de la section doit correspondre");
    }

    @Test
    @DisplayName("findByPortfoliotrueWithProjection devrait retourner une page de projections d'articles avec relations chargées")
    void shouldReturnPageOfCorrectlyMappedArticleProjections_whenFindByPortfolioTrueWithProjectionCalled() throws JsonProcessingException {

        // Arrange
        int page = 0;
        int size = 10;

        // Act
        Page<ArticleProjection> result = articleService.findByPortfoliotrueWithProjection(page, size);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(result);
        System.out.println("JSON qui serait envoyé par l'API : " + json);

        // Assert
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertFalse(result.isEmpty(), "L'objet de pagination ne doit pas être vide");

        assertNotNull(result.getContent().get(0).getSection(),
                "L'objet doit Section doit être récupérer par Hibernate.initialize()");

        assertEquals("Java", result.getContent().get(0).getSection().getDescription(),
                "La description de la section doit correspondre");
    }

}