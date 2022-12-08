package ArticleWebService.service;

import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleModel;
import com.jayway.jsonpath.Option;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ArticleService {

    Page<Article> findAllArticles(int page, int size );
    Optional<Article> findArticleById(Long id);

    boolean saveArticle(String article, List<MultipartFile> images);

    Option deleteArticle(ArticleModel article);

    String saveImage(MultipartFile file) throws Exception;

}
