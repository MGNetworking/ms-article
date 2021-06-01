package ghoverblog.ovh.blogarticle.web;

import ghoverblog.ovh.blogarticle.entities.Article;
import ghoverblog.ovh.blogarticle.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class ControllerArticle {

    private ArticleRepository articleRepository;

    public ControllerArticle(ArticleRepository articleRP) {
        this.articleRepository = articleRP;
    }

    @GetMapping("/getListArticle")
    public List<Article> articleList() {

        log.info("Get list articles");
        return articleRepository.findAll();
    }

    @PostMapping("/saveArticle")
    public Article saveArticle(@RequestBody Article article) {

        log.info("save articles");
        article.setDate(new Date());
        return articleRepository.save(article);
    }


}
