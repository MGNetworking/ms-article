package ArticleWebService.service;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleForm;
import ArticleWebService.entities.Domain;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ArticleService {

    /**
     * Fait une recherche tous les articles
     *
     * @param page le numéro de la page
     * @param size le nombre d'article
     * @return une pagination d'articleDTO
     */
    Page<ArticleDto> findArticlesPagination(int page, int size);

    /**
     * Fait une recherche d'article par leur section.
     *
     * @param page      le numéro de la page
     * @param size      le nombre d'article
     * @param sectionId la section visé
     * @return une pagination d'articleDTO par leur section
     */
    Page<ArticleDto> findArticlesPaginationSection(int page, int size, Integer sectionId);

    Optional<Article> findArticleById(Integer id) throws IllegalArgumentException ;

    /**
     * Sauvegarde un article
     *
     * @param articleForm
     * @return un objet ArticleDTO
     * @throws Exception dans le cas ou l'objet passé en paramétre et null.
     */
    ArticleForm saveArticle(ArticleForm articleForm) throws IllegalArgumentException;

    void deleteArticleById(Integer idArticle)  throws IllegalArgumentException ;

    List<Domain> getArticleWithSection();

}
