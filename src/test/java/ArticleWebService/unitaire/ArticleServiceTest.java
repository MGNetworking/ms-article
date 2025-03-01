package ArticleWebService.unitaire;

import ArticleWebService.entities.Article;
import ArticleWebService.handler.Exception.ArticleException;
import ArticleWebService.projection.ArticleProjection;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.repository.DomainRepository;
import ArticleWebService.service.ArticleServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import javax.persistence.EntityManager;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private EntityManager entityManager;

    @Test
    @DisplayName("Lève une exception ArticleException pour un ID invalide (article non trouvé)")
    public void findArticleById_ArticleExceptionTest() {

        // Arrange
        int invalidId = 9999;
        String message = String.format("L'identifiant de l'article : %d n'a pas été trouvé", invalidId);
        Mockito.when(this.articleRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert : Vérifie qu'une exception est levée
        ArticleException exception = Assertions.assertThrows(ArticleException.class, () -> {
            this.articleService.findArticleById(9999);
        });

        // Assert : Vérifie les détails de l'exception
        assertEquals(
                String.format("Message: '%s' status: %s", message, HttpStatus.NOT_FOUND),
                exception.getMessage(), "Le message d'exception devrait correspondre");

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        Mockito.verify(this.articleRepository, Mockito.times(1)).findById(invalidId);

    }

    @Test
    @DisplayName("Lève une exception ArticleException pour une erreur technique (DataAccessException)")
    void findArticleById_DataAccessExceptionTest() {
        // Arrange
        Integer id = 1;
        Mockito.when(articleRepository.findById(id))
                .thenThrow(new DataAccessException("Erreur technique simulée") {
                });

        // Act & Assert
        ArticleException exception = Assertions.assertThrows(ArticleException.class, () -> {
            articleService.findArticleById(id);
        });

        // Assert : Vérifie le message utilisateur de l'ArticleException
        Assertions.assertTrue(exception.getMessage().contains("Erreur technique : impossible de récupérer l'article."),
                "Le message d'exception utilisateur devrait contenir 'Erreur technique : impossible de récupérer l'article.'");

        // Vérifie le statut HTTP
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus(),
                "Le statut de l'exception devrait être INTERNAL_SERVER_ERROR.");

        // Vérifie que la méthode du repository a été appelée
        Mockito.verify(articleRepository, Mockito.times(1)).findById(id);
    }


    @Test
    @DisplayName("Retourne un entity Article pour un ID existant")
    void findArticleById_ReturnsArticleEntityTest() throws ArticleException {
        // Arrange
        Integer validId = 1; // Un ID valide
        Article mockArticle = new Article();
        mockArticle.setIdArticle(validId);
        mockArticle.setTitre("Article Test");
        mockArticle.setArticle("Contenu de l'article de test.");

        // Configure Mockito pour retourner un article
        Mockito.when(this.articleRepository.findById(validId)).thenReturn(Optional.of(mockArticle));

        // Act
        Article result = this.articleService.findArticleById(validId);

        // Assert
        assertNotNull(result, "L'article ne doit pas être null");
        assertEquals(validId, result.getIdArticle(), "L'ID de l'article doit correspondre");
        assertEquals("Article Test", result.getTitre(), "Le titre de l'article doit correspondre");
        assertEquals("Contenu de l'article de test.", result.getArticle(), "Le contenu de l'article doit correspondre");

        // Vérifie que le repository a bien été appelé
        Mockito.verify(this.articleRepository, Mockito.times(1)).findById(validId);
    }

}
