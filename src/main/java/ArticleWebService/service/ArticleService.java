package ArticleWebService.service;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ArticleService {

    Page<Article> findArticlesWithPages(int page, int size);

    Page<Article> findArticlesPagesWithSection(int page, int size , Integer sectionId);

    Optional<Article> findArticleById(Integer id);

    ArticleDto saveArticle(ArticleDto articleDto );

    void deleteArticleById(Integer idArticle) ;

}
