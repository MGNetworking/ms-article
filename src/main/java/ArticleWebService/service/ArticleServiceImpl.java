package ArticleWebService.service;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.Domain;
import ArticleWebService.entities.Section;
import ArticleWebService.feign.StorageRestClient;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.repository.DomainRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    public ArticleServiceImpl(ArticleRepository articleRepository,
                              StorageRestClient storageRestClient,
                              FileSystemStorageServiceImplementation fileSyStorSServiceImp ,
                              DomainRepository domainRepository) {

        this.articleRepository = articleRepository;
        this.storageRestClient = storageRestClient;
        this.domainRepository = domainRepository;
        this.fsssI = fileSyStorSServiceImp;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public Page<Article> findArticlesWithPages(int page, int size) {
        return this.articleRepository
                .findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<Article> findArticlesPagesWithSection(int page, int size, Integer sectionId) {
        return this.articleRepository
                .findAllArticlesBySection(PageRequest.of(page, size), sectionId);
    }

    @Override
    public Optional<Article> findArticleById(Integer id) {
        return this.articleRepository.findById(id);
    }


    @Override
    public ArticleDto saveArticle(ArticleDto articleDto) {

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // mapping
        Article article = modelMapper.map(articleDto, Article.class);
        article.getSection().setIdSection(articleDto.getIdSection());

        log.info("Sauvegarde de l'objet en base et retour du DTO ");

        return modelMapper.map(
                this.articleRepository.save(article),
                ArticleDto.class);

    }

    @Override
    public void deleteArticleById(Integer idArticle) {

        this.articleRepository.deleteById(idArticle);

    }

    @Override
    public List<Domain> getlistDomainWithSection(){

        List<Domain> domainList = this.domainRepository.findAllWithSections();
        domainList = domainList.stream().distinct().collect(Collectors.toList());

        return domainList;
    }

}