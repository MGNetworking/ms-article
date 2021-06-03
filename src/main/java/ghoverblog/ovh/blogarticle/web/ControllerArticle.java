package ghoverblog.ovh.blogarticle.web;

import ghoverblog.ovh.blogarticle.entities.Article;
import ghoverblog.ovh.blogarticle.entities.RegisterArticle;
import ghoverblog.ovh.blogarticle.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class ControllerArticle {

    private ArticleRepository articleRepository;
    private Environment environment;

    public ControllerArticle(ArticleRepository articleRP, Environment env) {
        this.articleRepository = articleRP;
        this.environment = env;
    }

    @GetMapping("/getListArticle")
    public List<Article> articleList() {

        log.info("Get list articles");
        return articleRepository.findAll();
    }

    @PostMapping("/saveArticle")
    public Article saveArticle(@RequestBody RegisterArticle registerArticle) {

        log.info("save articles");

        // faire le mapping de l'objet article et RegisterArticle
        Article article = new Article();
        article.setUserId(registerArticle.getUserId());
        article.setTitre(registerArticle.getTitre());
        article.setPath(registerArticle.getPath());

        // Get article pour la modification du path de l'image
        Article articleDb = articleRepository.save(article);

        // modification du nom de l'image par Id de l'article
        articleDb.setPath(articleDb.getArticleId().toString());

        // enregistrement dans les asset de spring l'image avec le nouveau nom

        // Ajout du path image sur le server apache via le service sftp


        return articleDb ;
    }


}
