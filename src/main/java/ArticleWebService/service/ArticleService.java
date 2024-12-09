package ArticleWebService.service;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.*;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

public interface ArticleService {

    /**
     * Fait une recherche tous les articles
     *
     * @param page le numéro de la page
     * @param size le nombre d'articles
     * @return une pagination d'articleDTO
     */
    Page<ArticleDto> findArticlesPagination(int page, int size);

    /**
     * Fait une recherche tous les articles
     *
     * @param page le numéro de la page
     * @param size le nombre d'articles
     * @return une pagination d'article dans l'ordre des ID
     */
    Page<ArticleDto> findAllArticlePageOrderBy(int page, int size);

    /**
     * Fait une recherche d'article par leur section.
     *
     * @param page      le numéro de la page
     * @param size      le nombre d'articles
     * @param sectionId la section visée
     * @return une pagination d'articleDTO par leur section
     */
    Page<ArticleDto> findArticlesPaginationSection(int page, int size, Integer sectionId);

    Optional<Article> findArticleById(Integer id);

    /**
     * Sauvegarde d'un article
     *
     * @param articleSave l'article a sauvegarder
     * @return un objet Article sauvegarder
     * @throws ArticleException si l'article n'a pas pu être Sauvegardé
     */
    Optional<Article> saveArticle(ArticleSave articleSave) throws ArticleException;

    /**
     * Met à jour l'article
     *
     * @param articleUpdate l'article à mettre à jour
     * @return un objet Article mise à jour
     * @throws ArticleException si l'article n'a pas pu être mis à jour
     */
    Optional<Article> updateArticle(ArticleUpdate articleUpdate) throws ArticleException;

    void deleteArticleById(Integer idArticle) throws IllegalArgumentException;

    List<Domain> getAllDomainWithSection();

}
