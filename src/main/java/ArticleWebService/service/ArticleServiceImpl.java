package ArticleWebService.service;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.*;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.repository.DomainRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        Page<Article> article = this.articleRepository
                .findAll(PageRequest.of(page, size));

        // Mapping de chaque article dans la page vers articleDTO
        Page<ArticleDto> articleDtoPage = article.map(art -> this.modelMapper.map(art, ArticleDto.class));

        return articleDtoPage;

    }

    @Override
    public Page<ArticleDto> findAllArticlePageOrderBy(int page, int size) {

        Page<Article> article = this.articleRepository
                .findAllArticlePageOrderBy(PageRequest.of(page, size));

        // Mapping de chaque article dans la page vers articleDTO
        Page<ArticleDto> articleDtoPage = article.map(art -> this.modelMapper.map(art, ArticleDto.class));

        return articleDtoPage;
    }

    @Override
    public Page<ArticleDto> findArticlesPaginationSection(int page, int size, Integer sectionId) {

        Page<Article> article = this.articleRepository
                .findAllArticlesBySection(PageRequest.of(page, size), sectionId);

        // Mapping de chaque article dans la page vers articleDTO
        Page<ArticleDto> articleDtoPage = article.map(art -> this.modelMapper.map(art, ArticleDto.class));

        return articleDtoPage;
    }

    /**
     * Recherche un article par son ID
     *
     * @param id type Integer
     * @return Renvoi un objet article qui contient tout les références.
     */
    @Override
    public Optional<Article> findArticleById(Integer id) {

        return this.articleRepository.findById(id);
    }

    @Override
    public Optional<Article> saveArticle(ArticleSave articleSave) throws Exception {

        log.info("Tire de l'article : " + articleSave.getTitre());
        log.info("Identifiant user : " + articleSave.getIdUser());

        Article article = this.modelMapper.map(articleSave, Article.class);

        return Optional.of(this.articleRepository.save(article));

    }

    @Override
    public Optional<Article> updateArticle(ArticleUpdate articleUpdate) throws ArticleException {

        log.info("Titre de l'article : " + articleUpdate.getTitre());
        log.info("Identifiant user : " + articleUpdate.getIdUser());

        //Article article = this.modelMapper.map(articleUpdate, Article.class);

        // Récupérer l'article de la base de données en utilisant son ID
        Optional<Article> optionalArticle = this.articleRepository.findById(articleUpdate.getIdArticle());


        if (optionalArticle.isPresent()) {

            Article art = optionalArticle.get();

            // Modification des valeur de l'objet
            art.setTitre(articleUpdate.getTitre());
            art.setArticle(articleUpdate.getArticle());
            art.setDescription(articleUpdate.getDescription());
            art.setImgDescription(articleUpdate.getImgDescription());
            art.setVisibiliter(articleUpdate.getVisibiliter());
            art.setDateMaj(Timestamp.valueOf(LocalDateTime.now()));

            // met à jour l'article en base de données
            Article updateArt = this.articleRepository.save(art);

            entityManager.flush();

            // Rafraîchir l'état de l'objet en mémoire avec les valeurs actuelles en base de données
            this.entityManager.refresh(updateArt);

            return Optional.of(updateArt);
        } else {
            throw new ArticleException("L'article avec l'ID " + articleUpdate.getIdArticle() +
                    " n'existe pas en base de données",
                    HttpStatus.NOT_FOUND);
        }
    }


    @Override
    public void deleteArticleById(Integer idArticle) throws IllegalArgumentException {
        this.articleRepository.deleteById(idArticle);
    }

    @Override
    public List<Domain> getAllDomainWithSection() {
        return this.domainRepository.findAll();
    }


}