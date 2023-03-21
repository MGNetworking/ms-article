package ArticleWebService.service;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleSave;
import ArticleWebService.entities.ArticleUpdate;
import ArticleWebService.entities.Domain;
import ArticleWebService.feign.StorageRestClient;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ArticleServiceImpl implements ArticleService {

    private ArticleRepository articleRepository;
    private StorageRestClient storageRestClient;
    private DomainRepository domainRepository;
    private FileSystemStorageServiceImplementation fsssI;
    private ModelMapper modelMapper;

    @Value("${file.upload-dir}")
    private String uriStore;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository,
                              StorageRestClient storageRestClient,
                              FileSystemStorageServiceImplementation fileSyStorSServiceImp,
                              DomainRepository domainRepository) {

        this.articleRepository = articleRepository;
        this.storageRestClient = storageRestClient;
        this.domainRepository = domainRepository;
        this.fsssI = fileSyStorSServiceImp;
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
    public Optional<Article> updateArticle(ArticleUpdate articleUpdate) throws Exception {

        log.info("Titre de l'article : " + articleUpdate.getTitre());
        log.info("Identifiant user : " + articleUpdate.getIdUser());

        Article article = this.modelMapper.map(articleUpdate, Article.class);
        article = this.entityManager.merge(article);

        return Optional.of(this.articleRepository.save(article));

    }


    @Override
    public void deleteArticleById(Integer idArticle) throws IllegalArgumentException {
        this.articleRepository.deleteById(idArticle);
    }

    @Override
    public List<Domain> getAllDomainWithSection(){
        return this.domainRepository.findAll();
    }

}