package ghoverblog.ovh.blogarticle.service;

import ghoverblog.ovh.blogarticle.entities.Article;

import java.util.List;

public interface ArticleService {

    Article saveArticle(Article article);
    void saveAll(List<Article> articleList);
    void deleteArticle(Article article);

}
