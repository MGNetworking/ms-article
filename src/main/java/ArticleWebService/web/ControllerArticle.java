package ArticleWebService.web;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleModel;
import ArticleWebService.service.ArticleService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.File;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

//@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/article")
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
    @ApiOperation(value = "Get articles list with pagable ")
    public ResponseEntity<Page<Article>> listArticle(@RequestParam(defaultValue = "0", name = "page") int page,
                                                     @RequestParam(defaultValue = "0", name = "size") int size) {

        if ((page < 0 || size <= 0)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Your parameter in correcte ");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService.findAllArticles(page, size));
    }

    @PostMapping(path = "/saveImages")
    public ResponseEntity<String> saveImage(@RequestParam(value = "images", required = false) MultipartFile images) throws IOException {


        if (images != null){
            log.info("images objet :" + images);
            log.info("Originiale Filem name : " + images.getOriginalFilename());
            log.info("Size : " + images.getSize());
            log.info("Content Type  :" + images.getContentType());
            log.info(String.valueOf(images.getResource()));
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("https://ghoverblog.ovh/images/101.jpg");

    }

    @DeleteMapping ("/removeImages")
    public ResponseEntity<String> removeImages(@RequestParam("images") MultipartFile images) throws IOException {

        log.info("images objet :" + images);

        log.info(images.getOriginalFilename());
        log.info("Content Type  :" + images.getContentType());
        log.info(String.valueOf(images.getResource()));


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(images.getOriginalFilename());

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

    @ApiOperation(value = "Creates an article", response = Article.class)
    @PostMapping(value = "/saveArticle",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> saveArticle(@RequestPart("images") List<MultipartFile> images,
                                              @RequestPart("formulaire") String article
    ) throws Exception {


        log.info("-----------------------------");
        log.info("Le formulaire : " + article.toString());



        for (MultipartFile multipartFile : images) {
            log.info("name : " + multipartFile.getName());
            log.info("Resource : " + multipartFile.getResource());
            log.info("Size : " + multipartFile.getSize());
            log.info("InputStream : " + multipartFile.getInputStream());
            log.info("nom du fichier  : " + multipartFile.getOriginalFilename());
            log.info("Bytes  : " + multipartFile.getBytes());


            String root = "/home/maxime/Developpement/ghoverblog/uploads/articles/images";
            Path path = Paths.get(root);

            try{

                this.articleService.saveArticle(article, images);

                Files.copy(multipartFile.getInputStream(),
                        path.resolve(multipartFile.getOriginalFilename()));
            }catch (Exception ex){
                log.error("Erreur upload " + multipartFile.getOriginalFilename());
            }


        }

        log.info("-----------------------------");


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(article);

        // TODO Validation article with articleModel object
/*
        // create object for mapping
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);


        // mapping to input data
        ArticleDto articleDto = modelMapper.map(articleModel, ArticleDto.class);
        //articleDto.setFileImage(file);

        if (this.articleService.saveArticle(articleDto)) {

            log.info("Response Ok ");
            return ResponseEntity
                    .status(HttpStatus.OK).body(articleModel);
        } else {

            log.error("Response " + HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).body(articleModel);
        }*/

    }


}
