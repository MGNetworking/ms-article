package ArticleWebService.web;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.entities.Article;
import ArticleWebService.response.ResponseHandler;
import ArticleWebService.service.ArticleService;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /**
     * Allow getting pagination of Articles
     *
     * @param page to primitive type int, number of page
     * @param size to primitive type int, number of article
     * @return An Object JsonPath of Articles
     */
    @GetMapping("/getAllArticles")
    @ApiOperation(value = "Get articles list with pagable ")
    public ResponseEntity<Page<Article>> listArticle(
            @RequestParam(defaultValue = "0", name = "page") int page,
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

            try {

                this.articleService.saveArticle(article, images);

                Files.copy(multipartFile.getInputStream(),
                        path.resolve(multipartFile.getOriginalFilename()));
            } catch (Exception ex) {
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


    /**
     * @param fileImages
     * @return
     * @throws ArticleNotFoundException
     */
    @PostMapping(path = "/saveImages")
    public ResponseEntity<Object> saveImage(@RequestParam(value = "images", required = false)
                                            MultipartFile fileImages) {

        String message = "l'enregistrement de l'images " + fileImages.getName() + " a échouer";

        if (fileImages != null && !fileImages.isEmpty()) {

            try {

                log.info("images objet :" + fileImages.getOriginalFilename());
                String newIpImages = this.fsssI.storeImage(fileImages);
                log.info("IP images : " + newIpImages);

                message = "l'enregistrement de l'image " + fileImages.getOriginalFilename() + " a été réalisé avec succès";
                return ResponseHandler.generateResponse(message, HttpStatus.CREATED, newIpImages);

            } catch (Exception ex) {

                log.error(message + ex.getMessage());
                log.error("Cause :" + ex.getCause());
                return ResponseHandler.generateResponse(message, HttpStatus.NOT_FOUND, null);

            }
        } else {
            message = "Votre object est null ou vide ";
            return ResponseHandler.generateResponse(message, HttpStatus.BAD_REQUEST, null);

            //return ResponseEntity.badRequest().body("Votre object est null");
        }


    }


    @PostMapping(path = "/saveImagesUrl")
    public ResponseEntity<Object> saveImageUrl(@RequestParam(value = "url", required = false)
                                               String urlImages) {

        String message;

        try {

            if (urlImages == null) {
                log.error("String is null");
                throw new Exception("String is null");
            }

            if (urlImages.isEmpty()) {
                log.error("String is Empty");
                throw new Exception("String is Empty");
            }

            // sauvegarde l'images de l'adresse IP
            String newIpImages = this.fsssI.storeImageWithURL(urlImages);
            log.info("IP images : " + newIpImages);

            return ResponseHandler.generateResponse(
                    "l'enregistrement de l'image via IP  a été réalisé avec succès ",
                    HttpStatus.CREATED,
                    newIpImages);


        } catch (IOException ioe) {

            message = "Une problème est survenu pendant l'enregistrement du fichier.";
            return ResponseHandler.generateResponse(message, HttpStatus.NOT_FOUND, urlImages);

        } catch (Exception ex) {

            // TODO message plus générale
            message = "L'adresse IP et manquante ";
            log.error(message + "url : " + urlImages);
            log.error(ex.getMessage());
            log.error(ex.getCause().toString());

            return ResponseHandler.generateResponse(message, HttpStatus.NOT_FOUND, urlImages);
        }


    }


    /**
     * @param nameImages
     * @return
     * @throws IOException
     */
    @DeleteMapping("/removeImages")
    public ResponseEntity<Object> removeImages(@RequestParam("nameImages") String nameImages) throws IOException {

        log.info("Name images to be delete : " + nameImages);

        String message = "la suppression de l'image " + nameImages + " a échouer";

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
                    "L'action de suppression a été mise en œuvre avec succès et aucune autre information ne sera fournie"
                    , HttpStatus.OK
                    , null);

        } catch (Exception ex) {

            log.error("Exception message : " + ex.getMessage());
            log.error("Exception cause :" + ex.getCause());
            return ResponseHandler.generateResponse(message, HttpStatus.NOT_FOUND, null);

        }


    }

}
