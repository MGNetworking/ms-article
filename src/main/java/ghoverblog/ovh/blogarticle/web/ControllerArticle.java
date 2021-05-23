package ghoverblog.ovh.blogarticle.web;

import ghoverblog.ovh.blogarticle.entities.Article;
import ghoverblog.ovh.blogarticle.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
//@CrossOrigin("http://ghoverblog")
//@CrossOrigin( origins = "http://localhost/*")
@Slf4j
public class ControllerArticle {

    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping("/getListArticle")
    public List<Article> articleList() {
        log.info("Get list articles");
        return articleRepository.findAll();
    }

    @PostMapping("/saveArticle")
    public Article saveArticle(@RequestBody Article article) {
        log.info("save articles");
        return articleRepository.save(article);
    }
}
