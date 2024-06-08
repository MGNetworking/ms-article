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
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

//@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/article")
public class ControllerArticle {

    @Autowired
    private ArticleService articleService;


    public ControllerArticle() {
    }


    /**
     * Récupère un article par son identifiant.
     *
     * @param id L'identifiant de l'article à récupérer. Ne doit pas être null, si non retourn un status 404.
     * @return Article L'article correspondant à l'identifiant spécifié.
     * @throws ArticleException Si l'article correspondant à l'identifiant n'est pas trouvé.
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
     * Récupère une liste paginée d'articles.
     *
     * @param page Le numéro de la page à récupérer (par défaut 0). Doit être un entier positif.
     * @param size Le nombre d'articles par page (par défaut 10). Doit être un entier positif.
     * @return ResponseEntity<Page < ArticleDto>> Un objet ResponseEntity contenant une page d'articles.
     * Le statut sera HttpStatus.OK en cas de succès.
     * @throws Exception Si une erreur survient lors de la récupération des articles paginés.
     */
    @GetMapping(path = "/getAllArticles")
    @ApiOperation(value = "Get articles list with pagable ")
    public ResponseEntity<Page<ArticleDto>> getlistArticlePagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) Integer page,
            @RequestParam(defaultValue = "10", name = "size", required = true) Integer size)
            throws Exception {

        // Validation des paramètres minimal
        page = page < 0 ? 0 : page;
        size = size < 1 ? 10 : size;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService.findArticlesPagination(page, size));
    }

    /**
     * Récupère une liste paginée d'articles pour une section donnée.
     *
     * @param page    Le numéro de la page à récupérer (par défaut 0). Doit être un entier positif.
     * @param size    Le nombre d'articles par page (par défaut 10). Doit être un entier positif.
     * @param section L'identifiant de la section pour laquelle récupérer les articles (par défaut 1).
     * @return ResponseEntity<Page < ArticleDto>> Un objet ResponseEntity contenant une page d'articles.
     * Le statut sera HttpStatus.OK en cas de succès.
     * @throws Exception Si une erreur survient lors de la récupération des articles.
     */
    @GetMapping(path = "/getAllArticlesSection")
    @ApiOperation(value = "Get articles list with section in pagable ")
    public ResponseEntity<Page<ArticleDto>> getlistArticleWithPagination(
            @RequestParam(defaultValue = "0", name = "page", required = true) Integer page,
            @RequestParam(defaultValue = "10", name = "size", required = true) Integer size,
            @RequestParam(defaultValue = "1", name = "sectionId", required = true) Integer section)
            throws Exception {

        // Validation des paramètres minimal
        page = page < 0 ? 0 : page;
        size = size < 1 ? 10 : size;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.articleService
                        .findArticlesPaginationSection(page, size, section));
    }


    /**
     * Sauvegarde les données d'un article dans le système.
     * Seuls les utilisateurs autorisés sont autorisés à effectuer cette opération.
     *
     * @param articleSave Les données de l'article à sauvegarder .
     *                    Les champs incluent le titre, le contenu et toute autre information pertinente.
     *                    L'identifiant de l'utilisateur doit être fourni pour des raisons de sécurité.
     * @return ResponseEntity<?> Un objet ResponseEntity contenant le résultat de la sauvegarde.
     * En cas de succès, le statut sera 201 et l'article créé sera retourné.
     * En cas d'erreur, le statut correspondant à l'erreur sera retourné avec un message d'erreur approprié.
     * Si la mise à jour d'un article est détectée via ce endpoint, le statut sera 403.
     * @throws Exception Si une erreur survient lors de la sauvegarde ou mise à jour de l'article.
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
                    "La mise à jour d'un article n'est pas autorisé à parti de ce point de terminaison",
                    "/article/saveArticle"));


        } else {

            log.info("Un nouvelle article va être créer");
            Article article = this.articleService.saveArticle(articleSave)
                    .orElseThrow(() -> new ArticleException(
                            String.format("L'article n° %d n'a pas été mise à jour ",
                                    articleSave.getIdArticle()),
                            HttpStatus.NOT_FOUND
                    ));

            String message = "L'article n° " + article.getIdArticle() + " à été créer avec succès ";
            log.info(message);

            return ResponseHandler.generateResponse(
                    message,
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
     * @return ResponseEntity<?> Un objet ResponseEntity contenant le résultat de la mise à jour.
     * En cas de succès, le statut sera 200 et l'article mis à jour sera retourné.
     * En cas d'erreur, le statut correspondant à l'erreur sera retourné avec un message d'erreur approprié.
     * Si la création d'un article est détectée via ce endpoint, le statut sera 403.
     * @throws ArticleException Si l'article n'existe pas ou ne peut pas être mis à jour.
     * @throws Exception        Si une erreur survient lors de la mise à jour de l'article.
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
                    "La création d'un article n'est pas autorisé à parti de ce point de terminaison",
                    "/article/updateArticle"));

        } else {

            Article article = this.articleService.updateArticle(articleUpdate)
                    .orElseThrow(() -> new ArticleException(
                            String.format("L'article n° %d n'a pas était mise à jour ",
                                    articleUpdate.getIdArticle()),
                            HttpStatus.NOT_FOUND
                    ));

            String message = "L'article n° " + articleUpdate.getIdArticle() + " va être mise à jour avec succès";
            log.info(message);

            return ResponseHandler.generateResponse(
                    message,
                    HttpStatus.CREATED,
                    article);
        }

    }

    /**
     * Supprime un article du système.
     * Seuls les utilisateurs autorisés sont autorisés à effectuer cette opération.
     *
     * @param idArticle L'identifiant de l'article à supprimer. Doit être un entier positif non nul.
     * @return ResponseEntity<?> Un objet ResponseEntity contenant le résultat de la suppression.
     * En cas de succès, le statut sera HttpStatus.OK et l'identifiant de l'article supprimé sera retourné.
     * En cas d'erreur, le statut HttpStatus.NOT_FOUND sera retourné avec un message d'erreur approprié.
     */
    @DeleteMapping(path = "/deleteArticle/{idArticle}")
    @PreAuthorize("@authentification.deleteArticle(#idArticle)")
    public ResponseEntity deteleArticle(@PathVariable @NotNull @Min(1) Integer idArticle) {

        try {

            this.articleService.deleteArticleById(idArticle);
            return ResponseHandler.generateResponse(
                    "La suppression de l'article a été réaliser avec succès "
                    , HttpStatus.OK
                    , idArticle);

        } catch (Exception ex) {

            log.error(ex.getMessage());
            return ResponseHandler.generateResponse(
                    "Impossible de trouver l'élément correspondant "
                    , HttpStatus.NOT_FOUND
                    , idArticle);

        }
    }

    /**
     * Récupère la liste de tous les domaines avec leurs sections associées.
     *
     * @return List<Domain> Une liste de tous les domaines avec leurs sections associées.
     * @throws Exception Si une erreur survient lors de la récupération des domaines avec sections.
     */
    @GetMapping(path = "/getAllDomain")
    public List<Domain> getAllDomain() {
        return this.articleService.getAllDomainWithSection();
    }

}