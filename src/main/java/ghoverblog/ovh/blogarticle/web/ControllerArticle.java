package ghoverblog.ovh.blogarticle.web;

import ghoverblog.ovh.blogarticle.entities.Article;
import ghoverblog.ovh.blogarticle.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/{id}")
    public @ResponseBody
    Optional getArticle(@PathVariable("id") String id) {

        log.info("recherche de l'article : " + id);
        return articleRepository.findById(Long.valueOf(id));
    }

    @PostMapping("/saveArticle")
    public Article saveArticle(@RequestBody Article article) {
        log.info("save articles");
        return articleRepository.save(article);
    }


}
