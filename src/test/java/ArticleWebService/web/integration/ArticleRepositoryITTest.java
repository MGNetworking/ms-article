package ArticleWebService.web.integration;

import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.Section;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.service.ArticleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cette classe de test est prévut pour utiliser une base de données H2
 */
@DataJpaTest
@Sql(scripts = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ArticleRepositoryITTest {

    @MockBean
    private ArticleService articleService; // Simule le bean pour éviter l'erreur

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Order(1)
    @DisplayName("Vérifie que les données sont bien présentes dans la BD H2")
    void verifyDataInH2Database() {
        // Récupérer toutes les données
        List<Article> articles = articleRepository.findAll();

        // Vérifications
        Assertions.assertFalse(articles.isEmpty(), "La base de données H2 ne contient pas d'articles.");
        articles.forEach(article ->
                System.out.println("Article ID: " + article.getIdArticle() + ", Titre: " + article.getTitre()));
    }


    @Test
    @Order(2)
    @DisplayName("Test la recherche d'un article par son ID")
    void findByIdTest() {
        // Utilisation de la méthode findById
        Optional<Article> article = articleRepository.findById(1);
        assertTrue(article.isPresent());
        assertNotNull(article);
        assertEquals(1, article.get().getIdArticle());
    }

    @Test
    @Order(3)
    @DisplayName("JPQL: Recherche la liste des artciles par leur section")
    void findAllArticlesBySectionTest() {

        Page<Article> page = this.articleRepository
                .findAllArticlesBySection(
                        PageRequest.of(0, 10),
                        1);

        assertNotNull(page, "La recherche de la liste n'à pas était trouver");
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size()); // Vérifie que la page contient 1 article
        assertEquals(1, page.getContent().get(0).getSection().getIdSection());
        assertEquals(1, page.getTotalPages());
    }

    @Test
    @Order(4)
    @DisplayName("JPQL: Recherche la liste des artciles")
    void findAllArticlePageOrderByTest() {

        Page<Article> page = this.articleRepository
                .findAllArticlePageOrderBy(PageRequest.of(0, 10));

        assertNotNull(page, "La recherche de la liste n'à pas était trouver");
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size()); // Vérifie que la page contient 1 article
        assertEquals(1, page.getTotalPages());
    }

    @Test
    @Order(5)
    @DisplayName("JPQL: Test la mise à jour de plusieurs champs d'un Article")
    void updateArticleFields() {

        Section section = new Section();
        section.setIdSection(2);

        ArticleDtoUpdate dto = new ArticleDtoUpdate();
        dto.setIdArticle(2);
        dto.setSection(section);
        dto.setTitre("Spring Boot advance");
        dto.setArticle("Contenu modifié");
        dto.setImgUrl("https://example.com/updated-image.png");
        dto.setImgDescription("Image modifiée");
        dto.setDescription("Description modifiée");

        int rowsUpdated = this.articleRepository.updateArticleFields(dto);
        // ArticleId correspond à la clé primaire composite
        Article updatedArticle = entityManager.find(Article.class, 2);

        // Vérification de la correspondance de l'article
        assertEquals(dto.getIdArticle(), updatedArticle.getIdArticle(), "Ce n'est pas le bon id article ");
        assertEquals(dto.getSection().getIdSection(), updatedArticle.getSection().getIdSection());
        assertEquals("Spring Boot advance", updatedArticle.getTitre(),
                "Le Titre n'est pas était identique a la modification");
        assertEquals("Contenu modifié", updatedArticle.getArticle(),
                "Le contenu de l'article n'est pas était identique a la modification");
        assertEquals("https://example.com/updated-image.png", updatedArticle.getImgUrl(),
                "La source de l'image n'est pas était identique a la modification");
        assertEquals("Image modifiée", updatedArticle.getImgDescription(),
                "La description de l'image n'est pas identique a la modification");
        assertEquals("Description modifiée", updatedArticle.getDescription(),
                "La description de l'article n'est pas identique a la modification");

        // Vérification des changements apportait
        assertEquals(1, rowsUpdated, "Une ligne devrait être mise à jour");

    }

    @Test
    @Order(6)
    @DisplayName("JPQL: Teste la mise à jours d'un champs d'un Article")
    void updateArticleField() {

        Section section = new Section();
        section.setIdSection(2);

        ArticleDtoUpdate dto = new ArticleDtoUpdate();
        dto.setIdArticle(2);
        dto.setSection(section);
        dto.setTitre("Spring Boot Teste");

        int rowsUpdated = this.articleRepository.updateArticleFields(dto);
        // ArticleId correspond à la clé primaire composite
        Article updatedArticle = entityManager.find(Article.class, 2);

        // Vérification de la correspondance de l'article
        assertEquals(dto.getIdArticle(), updatedArticle.getIdArticle(), "Ce n'est pas le bon id article ");
        assertEquals(dto.getSection().getIdSection(), updatedArticle.getSection().getIdSection());
        assertEquals("Spring Boot Teste", updatedArticle.getTitre(),
                "Le Titre n'est pas identique à la modification");

        // Vérification des changements apportait
        assertEquals(1, rowsUpdated, "Une ligne devrait être mise à jour");

    }

    @Test
    @Order(7)
    @DisplayName("JPQL: Teste la mise à jours des meta données d'un Article")
    void updateArticleMeta() {

        Section section = new Section();
        section.setIdSection(2);

        ArticleDtoUpdate dto = new ArticleDtoUpdate();
        dto.setIdArticle(2);
        dto.setSection(section);
        dto.setVisibale(false);
        dto.setVue(1);

        int rowsUpdated = this.articleRepository.updateArticleMeta(dto);
        // ArticleId correspond à la clé primaire composite
        Article updatedArticle = entityManager.find(Article.class, 2);

        // Vérification de la correspondance de l'article
        assertEquals(dto.getIdArticle(), updatedArticle.getIdArticle(), "Ce n'est pas le bon id article ");
        assertEquals(dto.getSection().getIdSection(), updatedArticle.getSection().getIdSection());

        // Vérification des changements apportait
        assertEquals(1, rowsUpdated, "Une ligne devrait être mise à jour");
        assertEquals(dto.getVue(), updatedArticle.getVue(),
                "La vue n'est pas était incrémenter de 1 ");
        assertFalse(dto.isVisibale(),
                "L'article ne doit pas être visible !");
    }

}