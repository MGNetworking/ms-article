package ArticleWebService.service;

import ArticleWebService.entities.Article;
import com.jayway.jsonpath.Option;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticleService {


    Page<Article> getAllArticlePageable(int page, int size );
    Optional<Article> getArticleById(Long id);

    Option PostArticle();
    Option PuttArticle();

    Option saveArticle(Article article);
    Option deleteArticle(Article article);

}
