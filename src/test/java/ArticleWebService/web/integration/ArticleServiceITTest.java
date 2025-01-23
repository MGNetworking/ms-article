package ArticleWebService.web.integration;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.service.ArticleServiceImpl;
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
    @DisplayName("Test succès : Retourne une page d'articles correctement mappés")
    void findArticlesPagination_ReturnsPageOfArticleDtos() {

        // Arrange
        int page = 0;
        int size = 10;

        // Act
        Page<ArticleDto> result = articleService.findArticlesPagination(page, size);

        // Assert
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertFalse(result.isEmpty(), "L'objet de pagination ne doit pas être vide");

        assertNotNull(result.getContent().get(0).getSection(),
                "L'objet doit Section doit être récupérer par Hibernate.initialize()");

        assertEquals("Java", result.getContent().get(0).getSection().getDescription(),
                "La description de la section doit correspondre");
    }

}