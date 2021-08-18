package ArticleWebService.service;

import ArticleWebService.entities.Article;
import ArticleWebService.repository.ArticleRepository;
import com.jayway.jsonpath.Option;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private ArticleRepository articleRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public Page<Article> getAllArticlePageable(int page, int size ) {
        return articleRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Optional<Article> getArticleById(Long id) {
        return this.articleRepository.findById(id);
    }

    @Override
    public Option PostArticle() {
        return null;
    }

    @Override
    public Option PuttArticle() {
        return null;
    }

    @Override
    public Option saveArticle(Article article) {
        return null;
    }

    @Override
    public Option deleteArticle(Article article) {
        return null;
    }
}