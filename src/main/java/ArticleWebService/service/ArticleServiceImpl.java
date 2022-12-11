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
    private FileSystemStorageServiceImplementation fsssI;

    @Value("${file.upload-dir}")
    private String uriStore;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, StorageRestClient storageRestClient,
                              FileSystemStorageServiceImplementation fileSyStorSServiceImp) {
        this.articleRepository = articleRepository;
        this.storageRestClient = storageRestClient;
        this.fsssI = fileSyStorSServiceImp;
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

    /**
     * Allows to save picture in local server
     *
     * @param file MultipartFile Picutre
     * @return String Ip adresse fo picture
     * @throws Exception
     */
    public String saveImage(MultipartFile file) throws Exception {

        try {

            return this.fsssI.storeImage(file);

        } catch (Exception ex) {
            log.error("Exception message: " + ex.getMessage());
            log.error("Exception cause : " + ex.getCause());
            throw new Exception(ex.getCause() + ex.getMessage());
        }

    }

    /**
     * Allows to delete a picutre in local server
     *
     * @param imagesName String name of pisutre
     * @return boolean to status of the transaction
     * @throws Exception
     */
    @Override
    public boolean deleteImages(String imagesName) throws Exception {
            return this.fsssI.deleteImages(imagesName);
    }


}