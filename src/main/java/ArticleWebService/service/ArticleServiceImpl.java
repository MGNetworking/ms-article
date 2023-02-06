package ArticleWebService.service;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.Section;
import ArticleWebService.feign.StorageRestClient;
import ArticleWebService.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ArticleServiceImpl implements ArticleService {

    private ArticleRepository articleRepository;
    private StorageRestClient storageRestClient;
    private FileSystemStorageServiceImplementation fsssI;
    private ModelMapper modelMapper;

    @Value("${file.upload-dir}")
    private String uriStore;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository,
                              StorageRestClient storageRestClient,
                              FileSystemStorageServiceImplementation fileSyStorSServiceImp) {

        this.articleRepository = articleRepository;
        this.storageRestClient = storageRestClient;
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
    public boolean deleteArticleById(Integer idArticle) {

        this.articleRepository.deleteById(idArticle);
        return true;
    }

}