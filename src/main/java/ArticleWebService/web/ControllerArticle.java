package ArticleWebService.web;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleForm;
import ArticleWebService.response.ResponseHandler;
import ArticleWebService.service.ArticleService;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;

//@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/article")
public class ControllerArticle {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private FileSystemStorageServiceImplementation fsssI;
    private ArticleDto articleDtoDate;

    private ControllerArticle() {
    }

    @PostMapping(path = "/saveArticle",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ApiOperation(value = "save articles in data base", response = ArticleDto.class)
    public ResponseEntity<ArticleDto> saveArticle(@Valid @RequestBody ArticleForm articleForm) {

        // Mapping model to DTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        ArticleDto articleDto = modelMapper.map(articleForm, ArticleDto.class);

        try {

            articleDto = this.articleService.saveArticle(articleDto);

            return new ResponseEntity<ArticleDto>(articleDto, HttpStatus.CREATED);

        } catch (ConcurrencyFailureException cfe) {
            log.error("une erreur de type ConcurrencyFailureException est survenu");
            log.error(cfe.getMessage());
            log.error(cfe.getCause().toString());
            return new ResponseEntity<ArticleDto>(articleDto, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("une erreur de type Exception est survenu");
            log.error(e.getMessage());
            log.error(e.getCause().toString());
            return new ResponseEntity<ArticleDto>(articleDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(path = "/deleteArticle/{id}")
    public ResponseEntity deteleArticle(@PathVariable @NotNull @Min(1) Integer id) {


        try {
            this.articleService.deleteArticleById(id);
            return ResponseHandler.generateResponse(
                    "La suppression de l'article a été réaliser avec succès "
                    , HttpStatus.OK
                    , id);

        } catch (IllegalArgumentException ex) {

            log.error(ex.getMessage());
            log.error(ex.getCause().toString());

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "impossible de trouver l'élément correspondant a l'identifiant : " + id);
        }
    }

    /**
     * Allow getting pagination of Articles
     *
     * @param page to primitive type int, number of page
     * @param size to primitive type int, number of article
     * @return An Object JsonPath of Articles
     */
    @GetMapping(path = "/getAllArticles")
    @ApiOperation(value = "Get articles list with pagable ")
    public ResponseEntity<Page<Article>> getlistArticlePagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) int page,
            @RequestParam(name = "size", required = true) @NotNull @Min(value = 1) int size) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService.findArticlesWithPages(page, size));
    }

    @GetMapping(path = "/getAllArticlesSection")
    @ApiOperation(value = "Get articles list with section in pagable ")
    public ResponseEntity<Page<Article>> getlistArticleWithPagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) int page,
            @RequestParam(defaultValue = "0", name = "size", required = true) int size,
            @RequestParam(defaultValue = "0", name = "sectionId") Integer section) {


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService
                        .findArticlesPagesWithSection(page, size, section));
    }


    /**
     * Allow getting an article by id
     *
     * @param id to Object type Long, id of Article
     * @return An Object JsonPath of Article
     * @throws ArticleNotFoundException
     */
    @GetMapping(path = "/getArticle/{id}")
    public Article getArticle(@PathVariable @NotNull Integer id) {

        return this.articleService
                .findArticleById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

    }


    /**
     * @param fileImages
     * @return
     */
    @PostMapping(path = "/saveImages")
    public ResponseEntity<Object> saveImage(@RequestParam(value = "images", required = true)
                                            @NotNull MultipartFile fileImages) {


        if ( !fileImages.isEmpty()) {

            try {

                log.info("images objet :" + fileImages.getOriginalFilename());
                String[] newIpImages = this.fsssI.storeImage(fileImages);

                log.info("Ip images : " + newIpImages[0]);
                log.info("name image : " + newIpImages[1]);

                return ResponseHandler.generateResponse(
                        "L'image " + newIpImages[0] + " a été réalisé avec succès a l'adresse suivant : "
                                + newIpImages[0],
                        HttpStatus.CREATED,
                        newIpImages);

            } catch (Exception ex) {

                log.error(ex.getMessage());
                log.error(ex.getCause().toString());

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Un problème technique est survenu ");
            }
        } else {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Votre object est null ou vide ");

/*            return ResponseHandler.generateResponse(
                    "Votre object est null ou vide ",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    null);*/
        }


    }

    /**
     * @param nameImages
     * @return
     * @throws IOException
     */
    @DeleteMapping("/deleteImages")
    public ResponseEntity<Object> deleteImages(@RequestParam("nameImages") String nameImages) throws IOException {

        log.info("Images a supprimer : " + nameImages);

        try {
            if (nameImages == null) {
                log.error("String is null");
                throw new Exception("String is null");
            }

            if (nameImages.isEmpty()) {
                log.error("String is Empty");
                throw new Exception("String is Empty");
            }

            this.fsssI.deleteImages(nameImages);

            return ResponseHandler.generateResponse(
                    "La suppression de l'images : " + nameImages + " a été réaliser avec succès "
                    , HttpStatus.OK
                    , nameImages);


        } catch (Exception ex) {

            log.error(ex.getMessage());
            log.error(ex.getCause().toString());

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "la suppression de l'image " + nameImages + " a échouer");


        }
    }
}