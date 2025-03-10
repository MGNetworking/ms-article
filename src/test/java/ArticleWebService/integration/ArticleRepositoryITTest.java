package ArticleWebService.integration;

import ArticleWebService.dto.ArticleDto;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cette classe de test est prévut pour utiliser une base de données H2
 */
@DataJpaTest
@Sql(scripts = {"/data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(encoding = "UTF-8"))
class ArticleRepositoryITTest {

    @MockBean
    private ArticleService articleService; // Simule le bean pour éviter l'erreur

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    @Order(1)
    @DisplayName("BD H2 : Vérifie que les données sont bien présentes dans la BD H2 de test")
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
    @DisplayName("JPQL : Recherche la liste des articles visible par leur section sans les portfolio")
    void findAllArticlesBySectionTest() throws JsonProcessingException {

        Page<Article> articlePage = this.articleRepository
                .findAllArticlesBySection(1,
                        true,
                        false,
                        PageRequest.of(0, 10));


        String json = new ObjectMapper().writeValueAsString(articlePage);
        System.out.println("JSON qui serait envoyé par l'API : " + json);

        assertFalse(articlePage.isEmpty(), "La page est vide");
        assertFalse(articlePage.getContent().isEmpty(), "La page devrait contenir au moins un article");
        assertEquals(0, articlePage.getNumber(), "La page retournée devrait être la page 0");
        assertEquals(10, articlePage.getSize(), "La taille de page demandée devrait être 10");
        assertTrue(articlePage.getNumberOfElements() <= 10, "Le nombre d'éléments ne devrait pas dépasser 10");

    }

    @Test
    @Order(3)
    @DisplayName("JPQL: Test la mise à jour de plusieurs champs d'un Article")
    void updateArticleFields() {

        Article article = this.articleRepository.findById(4).get();

        // modification des données
        article.setTitre("Spring Boot advance");
        article.setArticle("Contenu modifié");
        article.setImgUrl("https://example.com/updated-image.png");
        article.setImgDescription("Image modifiée");
        article.setDescription("Description modifiée");

        // Mappin vers un DTO puis update de l'objet
        ArticleDto articleDto = new ModelMapper().map(article, ArticleDto.class);
        int rowsUpdated = this.articleRepository.updateArticleFields(articleDto);

        // récupération de l'objet
        Article articleUpdate = this.articleRepository.findById(4).get();

        // Vérification
        assertEquals(1, rowsUpdated, "Une ligne devrait avoir était mise à jour");
        assertEquals(articleDto.getTitre(), articleUpdate.getTitre(),
                "Le Titre n'est pas était identique a la modification");
        assertEquals(articleDto.getArticle(), articleUpdate.getArticle(),
                "Le contenu de l'article n'est pas était identique a la modification");
        assertEquals(articleDto.getImgUrl(), articleUpdate.getImgUrl(),
                "La source de l'image n'est pas était identique a la modification");
        assertEquals(articleDto.getImgDescription(), articleUpdate.getImgDescription(),
                "La description de l'image n'est pas identique a la modification");
        assertEquals(articleDto.getDescription(), articleUpdate.getDescription(),
                "La description de l'article n'est pas identique a la modification");
    }


    @Test
    @Order(4)
    @DisplayName("JPQL: Teste la mise à jours des meta données d'un Article")
    void updateArticleMeta() {

        Article article = this.articleRepository.findById(2).get();
        boolean visibleArticle = article.isVisibale();
        boolean portfolioArticle = article.isPortfolio();

        article.setVue(article.getVue() + 1);           // ajoute 1
        article.setVisibale(!visibleArticle);           // inverse la valeur
        article.setPortfolio(!portfolioArticle);        // inverse la valeur

        ArticleDto articleDto = new ModelMapper().map(article, ArticleDto.class);

        // Mettre à jour l'article
        int rowsUpdated = this.articleRepository.updateArticleMeta(articleDto);

        // Récupérer l'article mis à jour avec votre repository
        Article articleUpdate = this.articleRepository.findById(2).get();


        // Vérifications
        assertEquals(rowsUpdated, 1, "Aucune ligne n'a était modifier ");
        assertEquals(articleDto.getIdArticle(), articleUpdate.getIdArticle(),
                "Ce n'est pas le bon id article ");
        assertEquals(1, rowsUpdated, "Une ligne devrait être mise à jour");
        assertEquals(articleDto.getVue(), articleUpdate.getVue(),
                "La vue n'est pas était incrémenter de 1 ");
        assertNotEquals(visibleArticle, articleUpdate.isVisibale(),
                "La visibilité de l'article n'a pas était modifier ");
    }

    @Test
    @Order(5)
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
    @Order(7)
    @DisplayName("JPQL : Recherche une pagination d'articles visible sans portfolio")
    void findAllPortfolioArticlesByVisibilityTest() throws JsonProcessingException {

        Page<Article> portfolioArticlesPage = this.articleRepository
                .findAllPortfolioArticlesByVisibility(true, false, PageRequest.of(0, 10));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(portfolioArticlesPage);
        System.out.println("JSON qui serait envoyé par l'API : " + json);

        // Assertions
        assertNotNull(portfolioArticlesPage, "La pagination et null ");
        assertFalse(portfolioArticlesPage.getContent().isEmpty(),
                "La liste des articles ne devrait pas être vide");
        List<Article> articlesList = portfolioArticlesPage.getContent();

        articlesList.forEach(article -> {
            assertTrue(article.isVisibale());       // Vérifie que les articles sont visibles
            assertFalse(article.isPortfolio());     // Vérifie que les portfolios ne sont pas présents
        });


        // Vérifier l'ordre croissant des ID (plus pertinent)
        if (portfolioArticlesPage.getContent().size() > 1) {
            List<Integer> ids = portfolioArticlesPage.getContent()
                    .stream()
                    .map(Article::getIdArticle)
                    .collect(Collectors.toList());

            assertThat(ids).isSorted(); // Vérifie que les IDs sont bien triés en ordre croissant
        }
    }

}