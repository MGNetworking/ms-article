package ArticleWebService.integration;

import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.Section;
import ArticleWebService.projection.ArticleProjection;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.service.ArticleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("BD H2: Vérifie que les données sont bien présentes dans la BD H2 de test")
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
    void findAllArticlesBySectionTest() throws JsonProcessingException {

        Page<Article> page = this.articleRepository
                .findAllArticlesBySection(
                        PageRequest.of(0, 10),
                        1);


        System.out.println("Content : " + page.getContent());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(page);
        System.out.println("JSON qui serait envoyé par l'API: " + json);

        assertNotNull(page, "La recherche de la liste n'à pas était trouver");
        assertEquals(4, page.getTotalElements());
        assertEquals(4, page.getContent().size()); // Vérifie que la page contient 1 article
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
        assertEquals(12, page.getTotalElements());
        assertEquals(10, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    @Order(5)
    @DisplayName("JPQL: Test la mise à jour de plusieurs champs d'un Article")
    void updateArticleFields() {

        Section section = new Section();
        section.setIdSection(1);

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
        assertEquals(dto.getSection().getIdSection(), updatedArticle.getSection().getIdSection(),
                "La section de l'article doit rester 1");
        assertEquals(dto.getTitre(), updatedArticle.getTitre(),
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
        section.setIdSection(1);

        ArticleDtoUpdate dto = new ArticleDtoUpdate();
        dto.setIdArticle(2);
        dto.setSection(section);
        dto.setTitre("Spring Boot Teste");

        int rowsUpdated = this.articleRepository.updateArticleFields(dto);
        // Vider le cache pour s'assurer d'avoir les données à jour
        entityManager.clear();
        // ArticleId correspond à la clé primaire composite
        Article updatedArticle = entityManager.find(Article.class, 2);

        // Vérification de la correspondance de l'article
        assertEquals(dto.getIdArticle(), updatedArticle.getIdArticle(),
                "Ce n'est pas le bon id article ");
        assertEquals(dto.getSection().getIdSection(), updatedArticle.getSection().getIdSection(),
                "La section de l'article doit rester 1");
        assertEquals("Spring Boot Teste", dto.getTitre(),
                "Le Titre n'est pas identique à la modification");
        assertEquals(1, rowsUpdated, "Une ligne devrait être mise à jour");

    }

    @Test
    @Order(7)
    @DisplayName("JPQL: Teste la mise à jours des meta données d'un Article")
    void updateArticleMeta() {

        Section section = new Section();
        section.setIdSection(1);

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
        assertEquals(dto.getSection().getIdSection(), updatedArticle.getSection().getIdSection(),
                "La section de l'article doit rester 1");

        // Vérification des changements apportait
        assertEquals(1, rowsUpdated, "Une ligne devrait être mise à jour");
        assertEquals(dto.getVue(), updatedArticle.getVue(),
                "La vue n'est pas était incrémenter de 1 ");
        assertFalse(dto.isVisibale(),
                "L'article ne doit pas être visible !");
    }

    @Test
    @Order(8)
    @DisplayName("Projection : Recherche une pagination d'articles avec portfolio a true")
    void findByPortfolioTrueOrderByIdArticleAscWithProjectionTest() throws JsonProcessingException {

        Page<ArticleProjection> portfolioArticlesPage = this.articleRepository
                .findByPortfolioTrueOrderByIdArticleAsc(PageRequest.of(0, 10), ArticleProjection.class);

        System.out.println("pagination projection : " + portfolioArticlesPage);
        System.out.println("pagination projection TotalPages: " + portfolioArticlesPage.getTotalPages());
        System.out.println("pagination projection TotalPages: " + portfolioArticlesPage.getTotalElements());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(portfolioArticlesPage);
        System.out.println("JSON qui serait envoyé par l'API : " + json);

        // Assertions
        assertNotNull(portfolioArticlesPage, "La recherche de portfolio articles a échoué");
        assertThat(portfolioArticlesPage.getTotalPages()).isEqualTo(2);
        assertFalse(portfolioArticlesPage.getContent().isEmpty(),
                "La liste des articles ne devrait pas être vide");

        // Vérifier l'ordre croissant des ID (plus pertinent)
        if (portfolioArticlesPage.getContent().size() > 1) {
            List<Integer> ids = portfolioArticlesPage.getContent()
                    .stream()
                    .map(ArticleProjection::getIdArticle)
                    .collect(Collectors.toList());

            assertThat(ids).isSorted(); // Vérifie que les IDs sont bien triés en ordre croissant

        }
    }

    @Test
    @Order(9)
    @DisplayName("Article Projection Dynamic : Recherche une pagination d'artciles avec portfolio a true")
    public void findByPortfolioTrueOrderByIdArticleAscWithDynamicProjectionTest() {
        // Créer et enregistrer des articles de test (comme ci-dessus)

        // Exécuter la méthode avec projection dynamique
        Page<ArticleProjection> portfolioArticlesPage =
                articleRepository.findByPortfolioTrueOrderByIdArticleAsc(
                        PageRequest.of(0, 10),
                        ArticleProjection.class
                );

        System.out.println("pagination projection : " + portfolioArticlesPage);
        System.out.println("pagination projection TotalPages: " + portfolioArticlesPage.getTotalPages());
        System.out.println("pagination projection Content: " + portfolioArticlesPage.getContent());

        // Vérifications
        assertThat(portfolioArticlesPage.getTotalElements()).isEqualTo(12);
    }

}