package ArticleWebService.service;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.*;
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
     * Fait une recherche tous les articles
     *
     * @param page le numéro de la page
     * @param size le nombre d'article
     * @return une pagination d'article dans l'ordre des ID
     */
    Page<ArticleDto> findAllArticlePageOrderBy(int page, int size);

    /**
     * Fait une recherche d'article par leur section.
     *
     * @param page      le numéro de la page
     * @param size      le nombre d'article
     * @param sectionId la section visé
     * @return une pagination d'articleDTO par leur section
     */
    Page<ArticleDto> findArticlesPaginationSection(int page, int size, Integer sectionId);

    Optional<Article> findArticleById(Integer id);

    /**
     * Sauvegarde d'un article
     *
     * @param articleSave
     * @return un objet Articl
     * @throws Exception dans le cas ou l'objet passé en paramétre et null.
     */
    Optional<Article> saveArticle(ArticleSave articleSave) throws Exception;

    Optional<Article> updateArticle(ArticleUpdate articleUpdate) throws Exception;

    void deleteArticleById(Integer idArticle) throws IllegalArgumentException;

    public List<Domain> getAllDomainWithSection();

}
