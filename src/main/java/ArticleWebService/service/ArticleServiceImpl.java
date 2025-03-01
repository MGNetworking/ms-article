package ArticleWebService.service;

import ArticleWebService.handler.Exception.ArticleException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.dto.ArticleDtoSave;
import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.*;
import ArticleWebService.projection.ArticleProjection;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.repository.DomainRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ArticleServiceImpl implements ArticleService {

    private ArticleRepository articleRepository;
    private DomainRepository domainRepository;
    private ModelMapper modelMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository,
                              DomainRepository domainRepository) {

        this.articleRepository = articleRepository;
        this.domainRepository = domainRepository;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public Page<ArticleDto> findArticlesPagination(int page, int size) {
        try {
            Page<Article> article = this.articleRepository
                    .findAll(PageRequest.of(page, size));

            // Charger explicitement les relations Lazy pour chaque entité
            article.getContent().forEach(
                    art -> Hibernate.initialize(art.getSection()));


            return article.map(art -> this.modelMapper.map(art, ArticleDto.class));

        } catch (DataAccessException ex) {
            String message = String
                    .format("Erreur lors de la récupération sans Ordre de la page %d élément %d  message %s",
                            page, size, ex.getMessage());

            log.error(message);
            throw new ArticleException(String.format(message), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * TODO utilisé
     * Permet de récupérer les données
     * @param page le numéro de la page.
     * @param size le nombre d'articles.
     * @return
     */
    @Override
    public Page<ArticleDto> findAllArticlePageOrderBy(int page, int size) {
        try {
            Page<Article> articleData = this.articleRepository
                    .findAllArticlePageOrderBy(PageRequest.of(page, size));

            // Charger explicitement les relations Lazy pour chaque entité
            articleData.getContent().forEach(
                    art -> Hibernate.initialize(art.getSection()));

            return articleData.map(article -> this.modelMapper.map(article, ArticleDto.class));
        } catch (DataAccessException ex) {
            String message = String
                    .format("Erreur lors de la récupération par Ordre de la page %d élément %d  message %s",
                            page, size, ex.getMessage());

            log.error(message);
            throw new ArticleException(String.format(message), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @Override
    public Page<ArticleDto> findArticlesPaginationSection(int page, int size, Integer sectionId) {
        try {

            Page<Article> article = this.articleRepository
                    .findAllArticlesBySection(
                            PageRequest.of(page, size), sectionId);

            // Charger explicitement les relations Lazy pour chaque entité
            article.getContent().forEach(
                    art -> Hibernate.initialize(art.getSection()));

            return article.map(art -> this.modelMapper.map(art, ArticleDto.class));
        } catch (DataAccessException ex) {

            String message = String
                    .format("Erreur lors de la récupération de la page %d élément %d section %s message %s",
                            page, size, sectionId, ex.getMessage());

            log.error(message);
            throw new ArticleException(String.format(message), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @Override
    public Page<ArticleProjection> findByPortfoliotrueWithProjection(int page, int size) {

        try {

            return this.articleRepository
                    .findByPortfolioTrueOrderByIdArticleAsc(PageRequest.of(page, size), ArticleProjection.class);
        } catch (DataAccessException ex) {

            String message = String
                    .format("Erreur lors de la récupération sur la projection d'article de la page %d élément %d section %s message %s",
                            page, size, ex.getMessage());

            log.error(message);
            throw new ArticleException(String.format(message), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Recherche un article par son ID
     *
     * @param id type Integer
     * @return Renvoi un objet article qui contient tout les références.
     */
    @Override
    public Article findArticleById(Integer id) throws ArticleException {
        try {
            Article article = articleRepository.findById(id)
                    .orElseThrow(() -> new ArticleException(
                            String.format("L'identifiant de l'article : %d n'a pas été trouvé", id),
                            HttpStatus.NOT_FOUND
                    ));

            // Chargement explicite de la relation Lazy section
            Hibernate.initialize(article.getSection());

            return article;

        } catch (DataAccessException ex) {
            //log.error("Erreur lors de l'accès à la base pour l'article ID: {}. Message: {}", id, ex.getMessage(), ex);
            throw new ArticleException("Erreur technique : impossible de récupérer l'article.",
                    HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Override
    public ArticleDto saveArticle(ArticleDtoSave articleDtoSave) {

        log.info("Titre de l'article : {}", articleDtoSave.getTitre());
        log.info("Identifiant user : {}", articleDtoSave.getIdUser());

        try {
            Article articleSave = this.articleRepository
                    .save(this.modelMapper.map(articleDtoSave, Article.class));
            return this.modelMapper.map(articleSave, ArticleDto.class);

        } catch (DataAccessException ex) {

            log.error("Erreur lors de l'insertion en base de données : {}", ex.getMessage());
            throw new ArticleException("Un problème technique est survenue pendant l'insertion de l'article",
                    HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    @Override
    public int updateArticleFields(ArticleDtoUpdate dto) throws ArticleException {

        int updatedRows = this.articleRepository.updateArticleFields(dto);

        if (updatedRows == 0) {
            throw new ArticleException("Aucun article mis à jour. Vérifiez l'ID ou les paramètres.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return updatedRows;
    }

    @Override
    public int updateArticleMeta(ArticleDtoUpdate dto) throws ArticleException {
        int updatedRows = this.articleRepository.updateArticleMeta(dto);

        if (updatedRows == 0) {
            throw new ArticleException("Aucune métadonnée de votre article n'a été mise à jour. " +
                    "Veuillez réessayer ultérieurement.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return updatedRows;
    }

    @Override
    public ArticleDto updateArticle(ArticleDtoUpdate articleDtoUpdate) throws ArticleException {

        log.info("Titre de l'article : {}", articleDtoUpdate.getTitre());
        log.info("Identifiant user : {}", articleDtoUpdate.getIdUser());
        log.info("Article url image : {}", articleDtoUpdate.getImgUrl());

        // Récupérer l'article en base de données en utilisant son ID
        Article article = this.articleRepository.findById(articleDtoUpdate.getIdArticle())
                .orElseThrow(() -> new ArticleException(
                        String.format("L'article avec l'ID %s n'existe pas en base de données",
                                articleDtoUpdate.getIdArticle()),
                        HttpStatus.NOT_FOUND));

        // Met à jour les données
        this.modelMapper.map(articleDtoUpdate, article);
        article.setDateMaj(Timestamp.valueOf(LocalDateTime.now()));

        // Enregistre en base de données
        Article updateArt = this.articleRepository.save(article);
        return this.modelMapper.map(updateArt, ArticleDto.class);
    }

    @Override
    public boolean deleteArticleById(Integer idArticle) {
        if (this.articleRepository.existsById(idArticle)) {
            this.articleRepository.deleteById(idArticle);
            return true;
        }
        return false;
    }

    @Override
    public List<Domain> getAllDomainWithSection() {
        return this.domainRepository.findAll();
    }


}