package ArticleWebService.service;

import ArticleWebService.handler.Exception.ArticleException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.dto.ArticleDtoSave;
import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.*;
import ArticleWebService.projection.ArticleProjection;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ArticleService {

    /**
     * Fait une recherche tous les articles.
     *
     * @param page le numéro de la page.
     * @param size le nombre d'articles.
     * @return une pagination d'articleDTO.
     */
    Page<ArticleDto> findArticlesPagination(int page, int size);

    /**
     * Fait une recherche tous les articles.
     *
     * @param page le numéro de la page.
     * @param size le nombre d'articles.
     * @return une pagination d'article dans l'ordre des ID.
     */
    Page<ArticleDto> findAllArticlePageOrderBy(int page, int size);

    /**
     * Fait une recherche d'article par leur section.
     *
     * @param page      le numéro de la page.
     * @param size      le nombre d'articles.
     * @param sectionId la section visée.
     * @return une pagination d'articleDTO par leur section.
     */
    Page<ArticleDto> findArticlesPaginationSection(int page, int size, Integer sectionId);

    Page<ArticleProjection> findByPortfoliotrueWithProjection(int page, int size);
    /**
     * Récuéper l'article complet par son ID
     *
     * @param id l'identifier de l'article.
     * @return un objet Article complet.
     */
    Article findArticleById(Integer id) throws ArticleException;

    /**
     * Sauvegarde d'un article.
     *
     * @param articleDtoSave l'article a sauvegarder.
     * @return un ArticleDto Article sauvegarder.
     * @throws ArticleException si l'article n'a pas pu être Sauvegardé.
     */
    ArticleDto saveArticle(ArticleDtoSave articleDtoSave) throws ArticleException;

    /**
     * Met à jour les champs d'un article.
     *
     * @param dto L'article à mettre à jour.
     * @return le nombre de lignes mise à jour, une seule ligne sera modifié et zéro en d'échec.
     * @throws ArticleException si l'article n'a pas pu être mise à jour
     */
    int updateArticleFields(ArticleDto dto) throws ArticleException;

    /**
     * Met à jour les meta données d'un article.
     *
     * @param dto l'article ciblé.
     * @return le nombre de lignes mise à jour, une seule ligne sera modifié et zéro en d'échec.
     * @throws ArticleException si les meta données de l'article n'a pas pu être mise à jour
     */
    int updateArticleMeta(ArticleDto dto) throws ArticleException;

    /**
     * Met à jour un article.
     *
     * @param articleDtoUpdate l'article à mettre à jour.
     * @return un objet Article mise à jour.
     * @throws ArticleException si l'article n'a pas pu être mis à jour.
     */
    ArticleDto updateArticle(ArticleDtoUpdate articleDtoUpdate) throws ArticleException;

    /**
     * Supprime un article.
     *
     * @param idArticle l'identifant de l'article.
     * @return un boolean qui status de la réussite de l'opération.
     */
    boolean deleteArticleById(Integer idArticle);

    /**
     * Permet d'obtenir la liste des domaines et section.
     *
     * @return une liste de domain.
     */
    List<Domain> getAllDomainWithSection();

}
