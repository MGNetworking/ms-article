package ArticleWebService.web;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.entities.ArticleUpdate;
import ArticleWebService.response.CustomerResponse;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleSave;
import ArticleWebService.entities.Domain;
import ArticleWebService.response.ResponseHandler;
import ArticleWebService.service.ArticleService;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ArticleWebService.tools.Authentification;

//@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/article")
public class ControllerArticle {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private FileSystemStorageServiceImplementation fsssI;

    public ControllerArticle() {
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
                .orElseThrow(() -> new ArticleException(
                        String.format("L'identifiant de l'article : %d  n'a pas était trouver", id),
                        HttpStatus.NOT_FOUND
                ));

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
    public ResponseEntity<Page<ArticleDto>> getlistArticlePagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) Integer page,
            @RequestParam(defaultValue = "10", name = "size", required = true) Integer size)
            throws Exception {

/*        page = page <= 0 ? 0 : page;
        size = size <= 0 ? 10 : size;*/

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService.findArticlesPagination(page, size));
    }

    /**
     * Permet d'obtenir une pagination d'articles.
     * En cas de demande de pages inexistante, une pagination vide sera renvoyer.
     *
     * @param page    le numéro de la page
     * @param size    le nombre d'article par pages
     * @param section la section et le type d'article
     * @return ResponseEntity<Page < Article> pagination des articles par la section
     */
    @GetMapping(path = "/getAllArticlesSection")
    @ApiOperation(value = "Get articles list with section in pagable ")
    public ResponseEntity<Page<ArticleDto>> getlistArticleWithPagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) Integer page,
            @RequestParam(defaultValue = "10", name = "size", required = true) Integer size,
            @RequestParam(defaultValue = "1", name = "sectionId", required = true) Integer section)
            throws Exception {

        // ternaire d'initialisation
        page = page < 0 ? 0 : page;
        size = size < 1 ? 10 : size;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService
                        .findArticlesPaginationSection(page, size, section));
    }


    /**
     * Permet uniquement la sauvegarde d'un article.
     *
     * @param articleSave classe de gestion du formulaire.
     * @return ResponseEntity<ArticleDto> l'article
     */
    @PostMapping(path = "/saveArticle",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ApiOperation(value = "Sauvegarde ou met a jour les données d'un article", response = Article.class)
    @PreAuthorize("@authentification.isAuthorization(#articleSave.idUser)")
    public ResponseEntity<Object> saveArticle(@Valid @RequestBody ArticleSave articleSave)
            throws Exception {

        // vérification de la nouveauté d'un article par son ID
        if (articleSave.statusArticle()) {

            log.info("la mise à jour d'un article ne doit pas être excuté appartir de ce endpoint");
            return ResponseHandler.generateResponse(new CustomerResponse(
                    HttpStatus.FORBIDDEN,
                    "Accès interdit",
                    "Vous n'êtes pas autorisé à mettre a jour un article " +
                            "a parti de ce point de terminaison",
                    "/article/saveArticle"));


        } else {

            log.info("Un nouvelle article va être créer");
            Article article = this.articleService.saveArticle(articleSave)
                    .orElseThrow(() -> new ArticleException(
                            String.format("L'article n° %d n'a pas était mise à jour ",
                                    articleSave.getIdArticle()),
                            HttpStatus.NOT_FOUND
                    ));

//            return new ResponseEntity<Object>(
//                    this.articleService.saveArticle(articleSave)
//                    , HttpStatus.CREATED);

            log.info("L'article n° " + article.getIdArticle() + " va être mise à jour ");
            return ResponseHandler.generateResponse(
                    "L'article n° " + article.getIdArticle() + " à été mise à jour ",
                    HttpStatus.CREATED,
                    article);
        }


    }

    /**
     * Met à jour les données d'un article dans le système.
     * Seuls les utilisateurs autorisés sont autorisés à effectuer cette opération.
     *
     * @param articleUpdate Les données de l'article à mettre à jour.
     *                      Les champs modifiables incluent le titre, le contenu et toute autre information pertinente.
     *                      L'identifiant de l'utilisateur doit être fourni pour des raisons de sécurité.
     *
     * @return ResponseEntity<Object> Un objet ResponseEntity contenant le résultat de la mise à jour.
     *                                En cas de succès, le statut sera HttpStatus.OK et l'article mis à jour sera retourné.
     *                                En cas d'erreur, le statut correspondant à l'erreur sera retourné avec un message d'erreur approprié.
     *
     * @throws Exception
     */
    @PutMapping(path = "/updateArticle",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ApiOperation(value = "Met à jour les données d'un article", response = Article.class)
    @PreAuthorize("@authentification.isAuthorization(#articleUpdate.idUser)")
    public ResponseEntity<?> updateArticle(@Valid @RequestBody ArticleUpdate articleUpdate)
            throws Exception {

        // vérification de l'ancienneté de l'article par la précence de son ID
        if (articleUpdate.statusArticle()) {

            log.info("la création d'un article ne doit pas être excuter a partir de ce endpoint ");
            return ResponseHandler.generateResponse(new CustomerResponse(
                    HttpStatus.FORBIDDEN,
                    "Accès interdit",
                    "Vous n'êtes pas autorisé à créer un article " +
                            "a parti de ce point de terminaison",
                    "/article/updateArticle"));

        } else {

            Article article = this.articleService.updateArticle(articleUpdate)
                    .orElseThrow(() -> new ArticleException(
                            String.format("L'article n° %d n'a pas était mise à jour ",
                                    articleUpdate.getIdArticle()),
                            HttpStatus.NOT_FOUND
                    ));

            log.info("L'article n° " + articleUpdate.getIdArticle() + " va être mise à jour ");
            return ResponseHandler.generateResponse(
                    "L'article n° " + articleUpdate.getIdArticle() + " à été mise à jour ",
                    HttpStatus.CREATED,
                    article);
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
     * Permet de enregistrer un fichier de type images
     *
     * @param fileImages
     * @return
     */
    @PostMapping(path = "/saveImages")
    public ResponseEntity saveImage(@RequestParam(value = "UploadFiles", required = true)
                                    @NotNull MultipartFile uploadFiles) {

        if (uploadFiles.isEmpty()) {

            return ResponseHandler.generateResponse(
                    "Fichier non présent",
                    HttpStatus.NOT_FOUND,
                    uploadFiles);
        }

//        Testing code for Front end
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.set("MyResponseHeader", "MyValue");
//        String[] imgSource = {"http://test.com", "NameTeste"};
//
//        Map<String, String> imgSrc = new HashMap<>();
//        imgSrc.put("data", "http://test.com");
//        imgSrc.put("name", "NameTeste");


//        return new ResponseEntity<Map<String, String>>(imgSrc, responseHeaders, HttpStatus.CREATED);

        try {

            log.info("Images recptionnée :" + uploadFiles.getOriginalFilename());
            String[] newIpImages = this.fsssI.storeImage(uploadFiles);
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
                    uploadFiles);

        }
    }

    @PostMapping("/upload")
    public String handleImageUpload(@RequestBody byte[] imageData) {
        // Votre logique pour gérer les données de l'image
        if (imageData != null && imageData.length > 0) {
            // Traitez les données de l'image ici, par exemple, enregistrez-les sur le serveur
            // Assurez-vous de gérer les exceptions et les erreurs potentielles
            return "Les données de l'image ont été téléchargées avec succès.";
        } else {
            return "Aucune donnée d'image reçue.";
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
    public ResponseEntity deleteImages(@RequestParam("nameImages") String nameImages) throws Exception {

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

    @GetMapping(path = "/getAllDomain")
    public List<Domain> getAllDomain() {
        return this.articleService.getAllDomainWithSection();
    }


}