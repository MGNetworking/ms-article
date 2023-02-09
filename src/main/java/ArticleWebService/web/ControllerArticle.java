package ArticleWebService.web;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleForm;
import ArticleWebService.entities.Domain;
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
import java.util.List;

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

    /**
     * Permet d'enregister un article et utilise un gestionnaire de validation.
     *
     * @param articleForm classe de gestion du formulaire.
     * @return ResponseEntity<ArticleDto> l'article
     */
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

    /**
     * Permet de supprimer un article par sont ID
     *
     * @param id
     * @return
     */
    @DeleteMapping(path = "/deleteArticle/{id}")
    public ResponseEntity deteleArticle(@PathVariable @NotNull @Min(1) Integer id) {

        try {

            this.articleService.deleteArticleById(id);
            return ResponseHandler.generateResponse(
                    "La suppression de l'article a été réaliser avec succès "
                    , HttpStatus.OK
                    , id);

        } catch (Exception ex) {

            log.error(ex.getMessage());
            return ResponseHandler.generateResponse(
                    "impossible de trouver l'élément correspondant "
                    , HttpStatus.NOT_FOUND
                    , id);

        }
    }

    /**
     * Permet d'obtenir la pagination des articles
     *
     * @param page le numéro de la page
     * @param size le nombre d'article par pages
     * @return ResponseEntity<Page < Article> pagination des articles
     */
    @GetMapping(path = "/getAllArticles")
    @ApiOperation(value = "Get articles list with pagable ")
    public ResponseEntity<Page<Article>> getlistArticlePagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) Integer page,
            @RequestParam(defaultValue = "10", name = "size", required = true) Integer size) {

        page = page <= 0 ? 0 : page;
        size = size <= 0 ? 10 : size;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService.findArticlesWithPages(page, size));
    }

    /**
     * Permet d'obtenir une pagination d'articles
     *
     * @param page    le numéro de la page
     * @param size    le nombre d'article par pages
     * @param section la section et le type d'article
     * @return ResponseEntity<Page < Article> pagination des articles par la section
     */
    @GetMapping(path = "/getAllArticlesSection")
    @ApiOperation(value = "Get articles list with section in pagable ")
    public ResponseEntity<Page<Article>> getlistArticleWithPagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) Integer page,
            @RequestParam(defaultValue = "10", name = "size", required = true) Integer size,
            @RequestParam(defaultValue = "1", name = "sectionId", required = true) Integer section) {

        page = page < 0 ? 0 : page;
        size = size < 1 ? 10 : size;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService
                        .findArticlesPagesWithSection(page, size, section));
    }


    /**
     * Permet de récupérer un article par son ID
     *
     * @param id
     * @return
     */
    @GetMapping(path = "/getArticle/{id}")
    public Article getArticle(@PathVariable @NotNull Integer id) {

        return this.articleService
                .findArticleById(id)
                .orElseThrow(() -> new ArticleException("L'article n'a pas était trouver"));

    }


    /**
     * Permet de enregistrer un fichier de type images
     *
     * @param fileImages
     * @return
     */
    @PostMapping(path = "/saveImages")
    public ResponseEntity<Object> saveImage(@RequestParam(value = "images", required = true)
                                            @NotNull MultipartFile fileImages) {


        if (fileImages.isEmpty()) {

            return ResponseHandler.generateResponse(
                    "Fichier non présent",
                    HttpStatus.NOT_FOUND,
                    fileImages);
        }

        try {

            log.info("Images recptionnée :" + fileImages.getOriginalFilename());
            String[] newIpImages = this.fsssI.storeImage(fileImages);
            log.info("Ip images : " + newIpImages[0]);
            log.info("name image : " + newIpImages[1]);

            String message = "L'image " + newIpImages[1] + " a été enregitre avec succès.";
            return ResponseHandler.generateResponse(
                    message,
                    HttpStatus.CREATED,
                    newIpImages);

        } catch (Exception ex) {

            log.error(ex.getMessage());
            log.error(ex.getCause().toString());

            return ResponseHandler.generateResponse(
                    "Un problème technique est survenu ",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    fileImages);

        }


    }

    /**
     * Permet de supprimer une images par son noms
     *
     * @param nameImages
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteImages")
    public ResponseEntity<Object> deleteImages(@RequestParam("nameImages") String nameImages) throws Exception {

        if (nameImages == null || nameImages.isEmpty()) {
            return ResponseHandler.generateResponse(
                    "Images non presente ",
                    HttpStatus.NOT_FOUND,
                    nameImages);
        }

        try {
            log.info("Images en cours de suppression : " + nameImages);
            this.fsssI.deleteImages(nameImages);
            String message = "La suppression de l'images : " + nameImages + " a été réaliser avec succès ";
            return ResponseHandler.generateResponse(message, HttpStatus.OK, nameImages);

        } catch (Exception ae) {

            return ResponseHandler.generateResponse(
                    "L'image n'a pas pu être supprimer ",
                    HttpStatus.NOT_FOUND,
                    nameImages);
        }

    }

    @GetMapping(path = "/getDomainList")
    public List<Domain> getDomainList() {
        return this.articleService.getlistDomainWithSection();
    }
}