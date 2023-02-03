package ArticleWebService.service;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ArticleService {

    Page<Article> findAllArticles(int page, int size);

    Optional<Article> findArticleById(Long id);

    ArticleDto saveArticle(ArticleDto articleDto );

    boolean deleteArticleById(Long idArticle);

}
