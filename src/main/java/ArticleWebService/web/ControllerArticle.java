package ArticleWebService.web;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.entities.Article;

import ArticleWebService.entities.ArticleDto;
import ArticleWebService.entities.ArticleModel;
import ArticleWebService.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Date;


@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class ControllerArticle {

    @Autowired
    private ArticleService articleService;

    /**
     * Allow getting pagination of Articles
     *
     * @param page to primitive type int, number of page
     * @param size to primitive type int, number of article
     * @return An Object JsonPath of Articles
     */
    @GetMapping("/getAllArticles")
    public ResponseEntity<Page<Article>> listArticle(@RequestParam(defaultValue = "0", name = "page") int page,
                                                     @RequestParam(defaultValue = "0", name = "size") int size) {

        if ((page < 0 || size <= 0)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Your parameter in correcte ");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService.findAllArticles(page, size));
    }

    /**
     * Allow getting an article by id
     *
     * @param id to Object type Long, id of Article
     * @return An Object JsonPath of Article
     */
    @GetMapping("/getArticle/{id}")
    public Article getArticle(@PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Error to id article");
        }

        return this.articleService
                .findArticleById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

    }

    @PostMapping(value = "/test")
    public ResponseEntity<String> savetest(@RequestParam("formulaire") MultipartFile[] formulaire) {


        return ResponseEntity.ok("Success Formulaire :" + formulaire );


    }

    //@RequestParam("image") MultipartFile file
    @PostMapping("/saveArticle")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleModel> saveArticle(@Valid
                                                    @RequestBody ArticleModel articleModel,
                                                    @RequestParam("image") MultipartFile file) {

        log.info("Article model : " + articleModel);
        log.info("Multipart file : " + file);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);


        // mapping to input data
        ArticleDto articleDto = modelMapper.map(articleModel, ArticleDto.class);
        articleDto.setFileImage(file);

        if (this.articleService.saveArticle(articleDto)) {

            log.info("Response Ok ");
            return ResponseEntity
                    .status(HttpStatus.OK).body(articleModel);
        } else {

            log.error("Response " + HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).body(articleModel);
        }

    }


}
