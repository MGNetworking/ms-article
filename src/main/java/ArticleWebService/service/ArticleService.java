package ArticleWebService.service;

import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleDto;
import ArticleWebService.entities.FileResponseClient;
import com.jayway.jsonpath.Option;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticleService {

    Page<Article> findAllArticles(int page, int size );
    Optional<Article> findArticleById(Long id);

    boolean saveArticle(ArticleDto article);
    Option deleteArticle(ArticleDto article);

}
