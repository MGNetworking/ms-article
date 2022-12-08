package ArticleWebService.service;

import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleModel;
import ArticleWebService.feign.StorageRestClient;
import ArticleWebService.repository.ArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ArticleServiceImpl implements ArticleService {

    private ArticleRepository articleRepository;
    private StorageRestClient storageRestClient;
    private FileSystemStorageServiceImplementation fileSyStorSServiceImp;

    @Value("${file.upload-dir}")
    private String uriStore;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, StorageRestClient storageRestClient,
                              FileSystemStorageServiceImplementation fileSyStorSServiceImp) {
        this.articleRepository = articleRepository;
        this.storageRestClient = storageRestClient;
        this.fileSyStorSServiceImp = fileSyStorSServiceImp;
    }

    @Override
    public Page<Article> findAllArticles(int page, int size) {
        return this.articleRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Optional<Article> findArticleById(Long id) {

        return this.articleRepository.findById(id);
    }


    @Override
    public boolean saveArticle(String article, List<MultipartFile> images) {

        // Mapping model to DTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        ObjectMapper objectMapper = new ObjectMapper();


        try {
            Article art = objectMapper.readValue(article, Article.class);

            log.info("Mon article mapper =>  " + art.getTitre());
        } catch (JsonProcessingException e) {
            log.error("Erreur Mapping article : " + e.getMessage());

            // TODO throw new Execption
        }

        return true;

    }

    @Override
    public Option deleteArticle(ArticleModel article) {

        return null;
    }

    public String saveImage(MultipartFile file) throws Exception {

        try {
            return this.fileSyStorSServiceImp.storeImage(file);

            // TODO sauvegarde adresse IP dans data Base

        } catch (Exception ex) {
            log.error("Erreur message: " + ex.getMessage());
            log.error("Erreur cause : " + ex.getCause());
            throw new Exception(ex.getCause() + ex.getMessage());
        }

    }
}