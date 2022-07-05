package ArticleWebService.service;

import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleModel;
import ArticleWebService.entities.FileResponseClient;
import ArticleWebService.feign.StorageRestClient;
import ArticleWebService.repository.ArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ArticleServiceImpl implements ArticleService {

    private ArticleRepository articleRepository;
    private StorageRestClient storageRestClient;

    @Value("${storage-article.location}")
    private String uriStore;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, StorageRestClient storageRestClient) {
        this.articleRepository = articleRepository;
        this.storageRestClient = storageRestClient;
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
            Article art =  objectMapper.readValue(article, Article.class);

            log.info("Mon article mapper =>  " + art.getTitre());
        } catch (JsonProcessingException e) {
            log.error("Erreur Mapping article : " + e.getMessage());
        }


        // Mapping dto Article
        //Article article = modelMapper.map(articleModel, Article.class);

        log.info("URI :" + this.uriStore);

        // TODO exception if image not found
        // add element of article (uri image and date)
        //article.setPath(this.uriStore + "/" + articleModel.getFileImage().getName());
        //article.setDate(new Date());

        // save article with transaction
        //this.articleRepository.save(article);

        // uploade image to micro service storage
        //FileResponseClient response = this.storageRestClient
        //        .uploadingImage(articleDto.getFileImage());

        //log.info("Response uploading :" + response.toString());

       // return response.getStatus().equals(HttpStatus.OK) ? true : false;

        return true;

    }

    @Override
    public Option deleteArticle(ArticleModel article) {

        return null;
    }
}