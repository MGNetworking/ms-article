package ArticleWebService.controler;


import ArticleWebService.handler.Exception.ArticleException;
import ArticleWebService.dto.ArticleDtoSave;
import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.*;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.handler.Exception.InvalidPathVariableException;
import ArticleWebService.handler.response.GenericApiResponse;
import ArticleWebService.handler.response.ResponseHandler;
import ArticleWebService.projection.ArticleProjection;
import ArticleWebService.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/articles")
public class ControllerArticle {

    private final ArticleService articleService;

    public ControllerArticle(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * Récupère un article par son identifiant.
     *
     * @param id      l'identifiant de l'article à récupérer. Ne doit pas être null.
     *                Retourne un statut HTTP 404 si l'article n'est pas trouvé.
     * @param request l'objet de requête HTTP en cours.
     * @return un objet ResponseEntity contenant une réponse API avec l'article correspondant (Article).
     * Le statut HTTP sera HttpStatus.OK si l'article est trouvé.
     * @throws ArticleException si aucun article correspondant à l'identifiant spécifié n'est trouvé.
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<GenericApiResponse<Article>> getArticle(
            @PathVariable("id") Integer id, HttpServletRequest request) {

        if (id < 0) {
            throw new InvalidPathVariableException(request.getRequestURI(), "Votre ID inférieur a 0");
        }

        return ResponseHandler.generateResponse(
                String.format("Demande sur l'article %d", id),
                HttpStatus.OK,
                request.getRequestURI(),
                this.articleService.findArticleById(id)
        );
    }

    /**
     * Récupère une liste paginée d'articles.
     *
     * @param page    le numéro de la page à récupérer (par défaut : 0). Doit être un entier positif.
     * @param size    le nombre d'articles par page (par défaut : 10). Doit être un entier positif.
     * @param request l'objet de requête HTTP en cours.
     * @return un objet ResponseEntity contenant une réponse API avec une page d'articles (ArticleDto).
     * Le statut HTTP sera HttpStatus.OK en cas de succès.
     */
    @GetMapping(path = "/list")
    public ResponseEntity<GenericApiResponse<Page<ArticleDto>>> getlistArticlePagination(
            @RequestParam(defaultValue = "0", name = "page") @Min(0) @Max(10) Integer page,
            @RequestParam(defaultValue = "10", name = "size") @Min(0) @Max(10) Integer size,
            HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                String.format("La page %d et le nombre d'éléments %d", page, size),
                HttpStatus.OK,
                request.getRequestURI(),
                this.articleService.findArticlesPagination(page, size)
        );
    }

    /**
     * Récupère une liste paginée des articles triés par identifiant.
     *
     * @param page    le numéro de la page (par défaut 0)
     * @param size    le nombre d'éléments par page (par défaut 10)
     * @param request l'objet de requête HTTP en cours
     * @return un objet de type ResponseEntity contenant une réponse API avec
     * une page d'articles (ArticleDto) triés par identifiant
     */
    @GetMapping(path = "/sorted")
    public ResponseEntity<GenericApiResponse<Page<ArticleDto>>> getlistArticlePaginationOrdreBy(
            @RequestParam(defaultValue = "0", name = "page") @Min(0) @Max(10) Integer page,
            @RequestParam(defaultValue = "10", name = "size") @Min(0) @Max(10) Integer size,
            HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                String.format("Page %d nombre d'éléments %d", page, size),
                HttpStatus.OK,
                request.getRequestURI(),
                this.articleService.findAllArticlePageOrderBy(page, size));

    }

    /**
     * Récupère une liste paginée d'articles pour une section donnée.
     *
     * @param page    Le numéro de la page à récupérer (par défaut 0). Doit être un entier positif.
     * @param size    Le nombre d'articles par page (par défaut 10). Doit être un entier positif.
     * @param section L'identifiant de la section pour laquelle récupérer les articles (par défaut 1).
     * @return ResponseEntity<Page < ArticleDto>> Un objet ResponseEntity contenant une page d'articles.
     * Le statut sera HttpStatus.OK en cas de succès.
     */
    @GetMapping(path = "/section")
    public ResponseEntity<GenericApiResponse<Page<ArticleDto>>> getlistArticleWithPagination(
            @RequestParam(defaultValue = "0", name = "page") @Min(0) @Max(10) Integer page,
            @RequestParam(defaultValue = "10", name = "size") @Min(0) @Max(10) Integer size,
            @RequestParam(defaultValue = "1", name = "sectionId") @Min(0) @Max(10) Integer section,
            HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                String.format("Page %d nombre d'élement %d", page, size),
                HttpStatus.OK,
                request.getRequestURI(),
                this.articleService.findArticlesPaginationSection(page, size, section));

    }

    /**
     * Récupère une liste paginée de portfolio.
     *
     * @param page
     * @param size
     * @param request
     * @return
     */
    @GetMapping(path = "/portfolio")
    public ResponseEntity<GenericApiResponse<Page<ArticleProjection>>> getPaginationArticleProjection(
            @RequestParam(defaultValue = "0", name = "page") @Min(0) @Max(10) Integer page,
            @RequestParam(defaultValue = "10", name = "size") @Min(0) @Max(10) Integer size,
            HttpServletRequest request) {
        return ResponseHandler.generateResponse(
                String.format("Page %d nombre d'élement %d", page, size),
                HttpStatus.OK,
                request.getRequestURI(),
                this.articleService.findByPortfoliotrueWithProjection(page, size));
    }


    /**
     * Sauvegarde les données d'un article dans le système.
     * Cette opération est réservée aux utilisateurs autorisés.
     *
     * @param articleDtoSave les données de l'article à sauvegarder.
     *                       Inclut le titre, le contenu et toute autre information pertinente.
     *                       L'identifiant de l'utilisateur (`idUser`) doit être fourni pour des raisons de sécurité.
     * @param request        l'objet de requête HTTP en cours.
     * @return un objet ResponseEntity contenant l'article sauvegardé avec un statut HTTP 200 (OK) en cas de succès.
     */
    @PostMapping(path = "/save",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("@access.isAuthorization(#articleDtoSave.idUser)")
    public ResponseEntity<GenericApiResponse<ArticleDto>> saveArticle(
            @Valid @RequestBody ArticleDtoSave articleDtoSave,
            HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                "L'article à été créer avec succès",
                HttpStatus.CREATED,
                request.getRequestURI(),
                this.articleService.saveArticle(articleDtoSave));


    }

    /**
     * Met à jour les données d'un article dans le système.
     * Cette opération est réservée aux utilisateurs autorisés.
     *
     * @param articleDtoUpdate les données de l'article à mettre à jour.
     *                         Les champs modifiables incluent le titre, le contenu et toute autre information pertinente.
     *                         L'identifiant de l'utilisateur (`idUser`) doit être fourni pour des raisons de sécurité.
     * @param request          l'objet de requête HTTP en cours.
     * @return un objet ResponseEntity contenant l'article mis à jour avec un statut HTTP 200 (OK) en cas de succès.
     */
    @PutMapping(path = "/update",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("@access.isAuthorization(#articleDtoUpdate.idUser)")
    public ResponseEntity<GenericApiResponse<ArticleDto>> updateArticle(
            @Valid @RequestBody ArticleDtoUpdate articleDtoUpdate,
            HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                "L'article a été mis à jour avec succès",
                HttpStatus.CREATED,
                request.getRequestURI(),
                this.articleService.updateArticle(articleDtoUpdate));

    }

    /**
     * Met à jour de manière partial les méta données d'un article.
     *
     * @param articleDto les données de l'article à mettre à jour.
     * @param request l'objet de requête HTTP en cours.
     * @return un objet ResponseEntity contenant le nombre de lignes mis à jour avec un statut HTTP 200 (OK) en cas de succès.
     */
    @PatchMapping(path = "/update/fields",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("@access.isAuthorization(#articleDto.idUser)")
    public ResponseEntity<GenericApiResponse<Integer>> updateArticleFields(
            @Valid @RequestBody ArticleDto articleDto,
            HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                "L'article a été mis à jour avec succès",
                HttpStatus.CREATED,
                request.getRequestURI(),
                this.articleService.updateArticleFields(articleDto));

    }

    @PatchMapping(path = "/update/meta",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("@access.isAuthorization(#articleDto.idUser)")
    public ResponseEntity<GenericApiResponse<Integer>> updateArticleMeta(
            @Valid @RequestBody ArticleDto articleDto,
            HttpServletRequest request) {

        return ResponseHandler.generateResponse(
                "L'article a été mis à jour avec succès",
                HttpStatus.CREATED,
                request.getRequestURI(),
                this.articleService.updateArticleMeta(articleDto));

    }


    /**
     * Supprime un article du système.
     * Cette opération est réservée aux utilisateurs autorisés.
     *
     * @param idArticle l'identifiant de l'article à supprimer. Doit être un entier positif non nul.
     * @param idUser    l'identifiant de l'utilisateur effectuant l'opération. Doit être un UUID valide.
     * @param request   l'objet de requête HTTP en cours.
     * @return un objet ResponseEntity contenant l'identifiant de l'article supprimé avec un statut HTTP 200 (OK) en cas de succès.
     */
    @DeleteMapping(path = "/delete/{idArticle}/{idUser}")
    @PreAuthorize("@access.isAuthorization(#idUser)")
    public ResponseEntity<GenericApiResponse<Integer>> deteleArticle(
            @PathVariable @NotNull @Min(1) Integer idArticle,
            @PathVariable @NotNull @Min(1) UUID idUser,
            HttpServletRequest request) {


        if (this.articleService.deleteArticleById(idArticle)) {
            return ResponseHandler.generateResponse(
                    "La suppression de votre article a été réaliser avec succès",
                    HttpStatus.OK,
                    request.getRequestURI(),
                    idArticle);
        } else {
            log.error("Échec de la suppression de l'article,Impossible de trouver la ressource");
            return ResponseHandler.generateResponse(
                    String.format("Impossible de trouver l'article correspondant à l'ID: %d", idArticle),
                    HttpStatus.NOT_FOUND,
                    request.getRequestURI(),
                    idArticle);

        }
    }

    /**
     * Récupère la liste de tous les domaines avec leurs sections associées.
     *
     * @return une liste de tous les domaines avec leurs sections associées.
     */
    @GetMapping(path = "/domain")
    public ResponseEntity<GenericApiResponse<List<Domain>>> getAllDomain(HttpServletRequest request) {
        return ResponseHandler.generateResponse(
                "La liste des Domain",
                HttpStatus.OK,
                request.getRequestURI(),
                this.articleService.getAllDomainWithSection()
        );
    }

}