package ArticleWebService.web;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.entities.Article;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.entities.DTOArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class ControllerArticle {

    @Autowired
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

    @GetMapping("/getListArticlePage")
    public Article listArticle(@RequestParam(name = "page") int page,
                               @RequestParam(name = "size") int size) {

        if ((page < 0 || size <= 0)) {
            log.error(String.format("page number %d", page));
            log.error(String.format("Elements Size %d", size));
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Your parameter in correcte ");
        }

        Pageable pageable = PageRequest.of(page, size);

        log.info("Pageable : "+ pageable);

        log.info("articleRepository : "+ articleRepository);

        return articleRepository
                .findAllArticles(pageable)
                .orElseThrow(() -> new ArticleNotFoundException("No items were found matching your search"));
    }

    @GetMapping("/getArticle")
    public Article getArticle(@RequestParam(value = "idArticle") Long idArticle) {
        log.info("Get article by id : " + idArticle);

        if (idArticle == null || idArticle <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Error to id article");
        }

        return articleRepository
                .findById(idArticle)
                .orElseThrow(() -> new ArticleNotFoundException(idArticle));

    }

    @PostMapping("/saveArticle")
    public Article saveArticle(@RequestBody DTOArticle DTOArticle) {

        log.info("save articles");

        // faire le mapping de l'objet article et RegisterArticle
        Article article = new Article();
        article.setUserId(DTOArticle.getUserId());
        article.setTitre(DTOArticle.getTitre());
        article.setPath(DTOArticle.getPath());

        // Get article pour la modification du path de l'image
        Article articleDb = articleRepository.save(article);

        // modification du nom de l'image par Id de l'article
        articleDb.setPath(articleDb.getArticleId().toString());

        // enregistrement dans les asset de spring l'image avec le nouveau nom

        // Ajout du path image sur le server apache via le service sftp


        return articleDb;
    }


}
