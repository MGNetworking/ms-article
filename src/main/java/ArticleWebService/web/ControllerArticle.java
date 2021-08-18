package ArticleWebService.web;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.entities.Article;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.entities.DTOArticle;

import ArticleWebService.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class ControllerArticle {

    @Autowired
    private ArticleService articleService;

    @GetMapping("/getPaginationArticle")
    public ResponseEntity<Page<Article>> listArticle(@RequestParam(defaultValue  = "0", name = "page") int page,
                                                     @RequestParam(defaultValue  = "0", name = "size") int size) {

        if ((page < 0 || size <= 0)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Your parameter in correcte ");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService.getAllArticlePageable(page, size));
    }

    @GetMapping("/getArticle")
    public Article getArticle(@RequestParam(required = false, value = "idArticle") Long articleId) {

        if (articleId == null || articleId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Error to id article");
        }

        return this.articleService
                .getArticleById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId));


    }

/*
    @PostMapping("/saveArticle")
    public Article saveArticle(@RequestBody DTOArticle DTOArticle) {

        log.info("save articles");

        // faire le mapping de l'objet article et RegisterArticle
        // TODO object Mapper
        // TODO Creation du service avec @Transactional
        Article article = new Article();
        article.setUserId(DTOArticle.getUserId());
        article.setTitre(DTOArticle.getTitre());
        article.setPath(DTOArticle.getPath());

        // Get article pour la modification du path de l'image
        Article articleDb = articleRepository.save(article);

        // modification du nom de l'image par Id de l'article
        articleDb.setPath(articleDb.getArticleId().toString());


        return articleDb;
    }
*/


}
