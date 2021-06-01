package ghoverblog.ovh.blogarticle.service;

import ghoverblog.ovh.blogarticle.entities.Article;
import ghoverblog.ovh.blogarticle.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private ArticleRepository articleRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public Article saveArticle(Article article) {

        return articleRepository.save(article);
    }

    @Override
    public void deleteArticle(Article article) {
        articleRepository.delete(article);

    }

    @Override
    public void saveAll(List<Article> articleList) {

        articleRepository.saveAll(articleList);
    }


}
