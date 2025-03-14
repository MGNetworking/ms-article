package ArticleWebService.endtoend;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.handler.Exception.ArticleException;
import ArticleWebService.entities.Article;
import ArticleWebService.dto.ArticleDtoSave;
import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.handler.response.GenericApiResponse;
import ArticleWebService.repository.ArticleRepository;
import ArticleWebService.SystemPropertiesActiveProfileResolver;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test des end point.
 * Il permet de test dans un environement contrôle le fonctionnement
 * des end points.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(resolver = SystemPropertiesActiveProfileResolver.class,
        profiles = {"test", "test-nas", "preprod", "prod"}) // Limité aux environnements spécifiques
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class ControllerArticleE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Value("${keycloak.auth-server-url}")
    private String keycloakURL;

    @Value("${keycloak.resource}")
    private String keycloakResource;

    @Value("${test.keycloak.user.one}")
    private String userOne;
    @Value("${test.keycloak.password.one}")
    private String passwordOne;
    private String idUserOne;
    private String tokenUserOne;

    @Value("${test.keycloak.user.two}")
    private String userTwo;
    @Value("${test.keycloak.password.two}")
    private String passwordTwo;
    private String idUserTwo;
    private String tokenUserTwo;

    // POST / DELETE
    private Integer articleId;
    private Timestamp dateCreation;

    @BeforeAll
    void setupToken() throws Exception {

        // Obtention d'un token valide pour les utilisateurs de test
        this.tokenUserOne = this.getAccessToken(this.userOne, this.passwordOne);
        this.tokenUserTwo = this.getAccessToken(this.userTwo, this.passwordTwo);

        // récupérer leur ID
        this.idUserOne = getIdUserAccessToken(tokenUserOne);
        this.idUserTwo = getIdUserAccessToken(tokenUserTwo);

    }

    @AfterAll
    void cleanupToken() {
        // Déconnexion des utilisateurs de test
        this.logout(this.tokenUserOne);
        this.logout(this.tokenUserTwo);
        log.info("Déconnexion des utilisateurs effectuée");
    }

    @Test
    @Order(1)
    @DisplayName("GET /article/{id} - Success")
    public void getArticleById() throws Exception {

        // Récupérer un article existant de la base de données
        Article articleFirst = this.articleRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));


        MvcResult result = this.mockMvc.perform(get("/articles/{id}", articleFirst.getIdArticle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andReturn();


        String responseJson = result.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(responseJson);
        int status = result.getResponse().getStatus();

        assertEquals(200, status, "Le statut HTTP attendu est incorrect.");
        assertTrue(jsonNode.has("timestamp"), "Le champ 'timestamp' devrait exister dans la réponse");

        // Mapping de la réponse pour des vérifications supplémentaires
        GenericApiResponse<Article> genericApiResponse = new ObjectMapper()
                .readValue(responseJson, new TypeReference<GenericApiResponse<Article>>() {
                });

        // Vérifiez que l'ApiResponse et l'Article ne sont pas null
        assertNotNull(genericApiResponse, "La réponse API ne doit pas être null");
        assertNotNull(genericApiResponse.getData(), "Les données de l'article ne doivent pas être null");

    }

    @Test
    @Order(2)
    @DisplayName("GET /articles/{id} - ID négatif")
    public void getArticleById_BadRequest() throws Exception {
        this.mockMvc.perform(get("/articles/{id}", -1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.path")
                        .value("/articles/-1"));
    }


    @Test
    @Order(3)
    @DisplayName("GET /articles/{id} - Invalid ID - MethodArgumentTypeMismatchException")
    public void testValidationErrors() throws Exception {
        this.mockMvc.perform(get("/articles/{id}", "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest()) // Vérifie que le statut est 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.message").isNotEmpty())
                .andExpect(jsonPath("$.data.info").isNotEmpty())
                .andExpect(result -> {
                            Throwable exception = result.getResolvedException();
                            Assertions.assertTrue(exception instanceof MethodArgumentTypeMismatchException);
                        }
                );
    }

    @Test
    @Order(4)
    @DisplayName("GET /articles/{id} - ID manquant")
    public void articleById_NullId() throws Exception {
        this.mockMvc.perform(get("/articles/") // ne rien mettre
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound()); // Vérifie le statut 404

    }

    @Test
    @Order(5)
    @DisplayName("GET /articles/{id} - Article Not found - ArticleException")
    public void articleById_NotFound() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/{id}", 5000))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.message").isNotEmpty())
                .andExpect(jsonPath("$.data.info").isNotEmpty())
                .andExpect(result -> {
                            Throwable exception = result.getResolvedException();
                            Assertions.assertTrue(exception instanceof ArticleException);
                        }
                );
    }

    @Test
    @Order(6)
    @DisplayName("GET /articles/list - Recherche reussi une pagination d'article status 200")
    public void articlesList_pageble_OK() throws Exception {

        int page = 0, size = 10;

        MvcResult result = this.mockMvc.perform(get("/articles/list")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        // Affichage de la réponse complète pour analyse
        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Réponse complète : " + responseContent);

        // Vérifiez manuellement si le statut est correct
        assertEquals(200, result.getResponse().getStatus(), "Le statut HTTP attendu est incorrect.");

        // Assertions détaillées avec messages personnalisés
        assertNotNull(responseContent, "La réponse ne doit pas être nulle.");

        // Utilisation de JsonPath pour des assertions plus précises
        DocumentContext jsonContext = JsonPath.parse(responseContent);

        // Vérifie l'existence de clés principales
        assertNotNull(jsonContext.read("$.timestamp"), "Le champ 'timestamp' est manquant.");

        assertEquals(
                String.format("La page %d et le nombre d'éléments %d", page, size),
                jsonContext.read("$.message"),
                "Le message de confirmation est incorrect."
        );

        assertTrue(jsonContext.read("$.data.content") instanceof List, "Le champ 'content' devrait être une liste.");
        assertFalse(((List<?>) jsonContext.read("$.data.content")).isEmpty(), "Le champ 'content' ne devrait pas être vide.");
        assertNotNull(jsonContext.read("$.data.pageable"), "Le champ 'pageable' est manquant.");
    }

    @Test
    @Order(7)
    @DisplayName("GET /articles/list - Success")
    public void articlesList_pageble_IsEmpty() throws Exception {

        int page = 99, size = 66;

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/list")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message")
                        .value(String.format("La page %d et le nombre d'éléments %d", page, size)))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/articles/list"))
                .andExpect(jsonPath("$.data.content").isArray()) // Vérifie que 'content' est un tableau
                .andExpect(jsonPath("$.data.content").isEmpty()) // Vérifie que 'content' est vide
                .andExpect(jsonPath("$.data.pageable").exists()) // Vérifie que 'pagination' existe
                .andDo(print());
    }

    @Test
    @Order(8)
    @DisplayName("GET /articles/section - Success")
    public void articleSection_pageble_OK() throws Exception {

        int page = 0, size = 6, section = 1;
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/section")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sectionId", String.valueOf(section)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/articles/section"))
                .andExpect(jsonPath("$.data.content").isArray()) // Vérifie que 'content' est un tableau
                .andExpect(jsonPath("$.data.content").isNotEmpty()) // Vérifie que 'content' est vide
                .andExpect(jsonPath("$.data.pageable").exists()) // Vérifie que 'pagination' existe
                .andDo(print());

    }

    @Test
    @Order(9)
    @DisplayName("GET /articles/section - Success")
    public void articleSection_pageble_IsEmpty() throws Exception {

        int page = 99, size = 66, section = 1;
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/section")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sectionId", String.valueOf(section)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/articles/section"))
                .andExpect(jsonPath("$.data.content").isArray()) // Vérifie que 'content' est un tableau
                .andExpect(jsonPath("$.data.content").isEmpty()) // Vérifie que 'content' est vide
                .andExpect(jsonPath("$.data.pageable").exists()) // Vérifie que 'pagination' existe
                .andDo(print());

    }

    @Test
    @Order(10)
    @DisplayName("GET /articles/portfolio - Success")
    public void articleportfolio_pageble_OK() throws Exception {

        int page = 0, size = 6, section = 1;
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/portfolio")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(MockMvcResultHandlers.print())
                // Vérification du statut et structure de base de la réponse
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Page %d nombre d'élement %d", page, size)))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/articles/portfolio"))

                // Vérification de la structure des données de pagination
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())  // Vérifie que des articles sont présents
                .andExpect(jsonPath("$.data.content.length()").value(size))  // Vérifie que le nombre d'articles correspond à la taille demandée

                // Vérification des métadonnées de pagination
                .andExpect(jsonPath("$.data.pageable").exists())
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.data.totalPages").exists())
                .andExpect(jsonPath("$.data.number").value(page))  // Vérifie que c'est bien la page demandée

                // Vérification de la structure d'un article (premier élément)
                .andExpect(jsonPath("$.data.content[0].idArticle").exists())
                .andExpect(jsonPath("$.data.content[0].titre").exists())
                .andExpect(jsonPath("$.data.content[0].description").exists())
                .andExpect(jsonPath("$.data.content[0].portfolio").value(true))  // Vérifie que c'est bien un article du portfolio

                .andDo(print());
        ;

    }

    @Test
    @Order(11)
    @DisplayName("GET /articles/portfolio - Success")
    public void articleportfolio_pageble_IsEmpty() throws Exception {

        int page = 99, size = 66, section = 1;
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/portfolio")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Page %d nombre d'élement %d", page, size)))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/articles/portfolio"))
                .andExpect(jsonPath("$.data.content").isArray()) // Vérifie que 'content' est un tableau
                .andExpect(jsonPath("$.data.content").isEmpty()) // Vérifie que 'content' est vide
                .andExpect(jsonPath("$.data.pageable").exists()) // Vérifie que 'pagination' existe
                .andDo(print());

    }

    @Test
    @Order(12)
    @DisplayName("GET /articles/domain - Success")
    public void getAllDomain() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/domain"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(20)
    @DisplayName("GET /articles/sorted asc - Success")
    public void articlesSorted_pageble_Sort_asc() throws Exception {

        int page = 0, size = 10;
        String sort = "asc";
        boolean visibility = true;

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/sorted")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .param("visibility", Boolean.toString(visibility))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/articles/sorted"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable").exists())
                .andDo(print());
    }

    @Test
    @Order(21)
    @DisplayName("GET /articles/sorted desc - Success")
    public void articlesList_pageble_Sort() throws Exception {

        int page = 0, size = 10;
        String sort = "desc";
        boolean visibility = false;

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/articles/sorted")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .param("visibility", Boolean.toString(visibility))
                        .param("sort", sort)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/articles/sorted"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable").exists())
                .andDo(print());
    }

    @Test
    @Order(13)
    @DisplayName("POST /articles/save - Success")
    @Transactional
        // Cette annotation permet de maintenir la session Hibernate ouverte pendant le test
    void shouldCreateArticleWhenValidRequest() throws Exception {

        // Récupérer un article pour le test
        Article article = this.articleRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // Charger explicitement les relations Lazy pour chaque entité
        Hibernate.initialize(article.getSection());

        // Mapping vers objet de sauvegarde puis je change sont ID pour créer un nouvel article
        ArticleDtoSave dtoSave = new ModelMapper().map(article, ArticleDtoSave.class);
        dtoSave.setIdUser(this.idUserOne);

        String json = new ObjectMapper().writeValueAsString(dtoSave);

        // Exécuter l'appel à l'API et capturer la réponse
        MvcResult mvcResult = this.mockMvc.perform(post("/articles/save")
                        .header("Authorization", "Bearer " + this.tokenUserOne) // Ajout du token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))             // transmisiton du Json au format String
                .andExpect(status().isCreated())    // check statut attendu 201
                .andDo(print())                     // Affiche la réponse dans la console
                .andReturn();                       // Récupère le résultat

        // Extraire le contenu de la réponse
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        log.info("Json Response : {} ", jsonResponse);

        // Convertion de la réponse
        JsonNode responseJson = new ObjectMapper().readTree(jsonResponse);
        this.articleId = responseJson.get("data").get("idArticle").asInt();
        String date = responseJson.get("data").get("dateCreation").asText();

        // Vérification
        assertNotNull(this.articleId, "L'ID de l'article ne doit pas être null");
        assertNotNull(date, "La date de création est absente ");
    }

    @Test
    @Order(14)
    @DisplayName("DELETE /articles/delete/{idArticle}/{idUser} - Is Forbidden")
    void shouldReturnFalseAfterDeleteArticle() throws Exception {

        this.mockMvc.perform(delete("/articles/delete/{idArticle}/{idUser}",
                        this.articleId, this.idUserOne)
                        .header("Authorization", "Bearer " + this.tokenUserTwo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @Order(15)
    @DisplayName("DELETE /articles/delete/{idArticle}/{idUser} - Success")
    void shouldReturnTrueAfterDeleteArticle() throws Exception {

        this.mockMvc.perform(delete("/articles/delete/{idArticle}/{idUser}",
                        this.articleId, this.idUserOne)
                        .header("Authorization", "Bearer " + this.tokenUserOne)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(16)
    @DisplayName("POST /articles/save - Forbidden")
    @Transactional
    void shouldReturnForbiddenWhenSaveArticle() throws Exception {

        // récupérer un artcile pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // chargement des données Lazy
        Hibernate.initialize(article.getSection());

        // Mapping vers objet de sauvegarde puis transformation en Json String
        ArticleDtoSave dtoSave = new ModelMapper().map(article, ArticleDtoSave.class);
        String json = new ObjectMapper().writeValueAsString(dtoSave);

        mockMvc.perform(post("/articles/save")
                        .header("Authorization", "Bearer " + tokenUserTwo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @Order(17)
    @DisplayName("POST /articles/save - Unauthoriezed")
    @Transactional
    void shouldReturnUnauthorizedWhenSaveArticle() throws Exception {

        // récupérer un artcile pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // chargement des données Lazy
        Hibernate.initialize(article.getSection());

        // Mapping vers objet de sauvegarde puis transformation en Json String
        ArticleDtoSave dtoSave = new ModelMapper().map(article, ArticleDtoSave.class);
        String json = new ObjectMapper().writeValueAsString(dtoSave);

        mockMvc.perform(post("/articles/save")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @Order(18)
    @DisplayName("PUT /articles/update - Is created")
    @Transactional
    void shouldReturnCreateWhenUpdatingArticle() throws Exception {

        // récupérer un artcile pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // chargement des données Lazy
        Hibernate.initialize(article.getSection());

        // Mapping vers objet de sauvegarde puis transformation en Json String
        ArticleDtoUpdate dtoUpdate = new ModelMapper().map(article, ArticleDtoUpdate.class);
        String json = new ObjectMapper().writeValueAsString(dtoUpdate);

        // Appel le end point
        mockMvc.perform(put("/articles/update")
                        .header("Authorization", "Bearer " + this.tokenUserOne)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andDo(print());
    }


    @Test
    @Order(19)
    @DisplayName("PUT /articles/update - Forbidden")
    @Transactional
    void shouldReturnForbiddenWhenUpdatingArticle() throws Exception {

        // récupérer un artcile pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // chargement des données Lazy
        Hibernate.initialize(article.getSection());

        // Mapping vers objet de sauvegarde puis transformation en Json String
        ArticleDtoUpdate dtoUpdate = new ModelMapper().map(article, ArticleDtoUpdate.class);
        String json = new ObjectMapper().writeValueAsString(dtoUpdate);

        // Appel le end point
        mockMvc.perform(put("/articles/update")
                        .header("Authorization", "Bearer " + this.tokenUserTwo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @Order(22)
    @DisplayName("PATCH /articles/update/fields - Success")
    @Transactional
    void shouldUpdateArticleFieldsWhenAuthorized() throws Exception {
        // Récupérer un article pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // Chargement des données Lazy
        Hibernate.initialize(article.getSection());

        // Mapping vers l'objet DTO
        ArticleDto articleDto = new ModelMapper().map(article, ArticleDto.class);
        articleDto.setIdUser(this.idUserOne);

        // Modification des champs à mettre à jour
        articleDto.setTitre("Nouveau titre mis à jour via PATCH");
        articleDto.setDescription("Nouvelle description mise à jour via PATCH");

        String json = new ObjectMapper().writeValueAsString(articleDto);

        // Appel du endpoint
        mockMvc.perform(patch("/articles/update/fields")
                        .header("Authorization", "Bearer " + this.tokenUserOne)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("L'article a été mis à jour avec succès"))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data").isNumber())
                .andDo(print());
    }

    @Test
    @Order(23)
    @DisplayName("PATCH /articles/update/fields - Forbidden")
    @Transactional
    void shouldReturnForbiddenWhenUpdatingArticleFields() throws Exception {

        // Récupérer un article pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // Chargement des données Lazy
        Hibernate.initialize(article.getSection());

        // Mapping vers l'objet DTO
        ArticleDto articleDto = new ModelMapper().map(article, ArticleDto.class);
        articleDto.setIdUser(this.idUserOne);
        articleDto.setTitre("");

        String json = new ObjectMapper().writeValueAsString(articleDto);

        // Appel du endpoint avec le token du deuxième utilisateur
        mockMvc.perform(patch("/articles/update/fields")
                        .header("Authorization", "Bearer " + this.tokenUserTwo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @Order(24)
    @DisplayName("PATCH /articles/update/meta - Success")
    @Transactional
    void shouldUpdateArticleMetaWhenAuthorized() throws Exception {
        // Récupérer un article pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // Mapping vers l'objet DTO
        ArticleDto articleDto = new ModelMapper().map(article, ArticleDto.class);

        // Définir les modifications de métadonnées
        articleDto.setVue(1); // Incrémenter le nombre de vues
        articleDto.setVisibale(false); // Modifier la visibilité
        articleDto.setPortfolio(false); // Ajouter au portfolio

        // Vérifier la mise à jour via la méthode du repository
        int updatedRows = articleRepository.updateArticleMeta(articleDto);
        assertThat(updatedRows).isEqualTo(1);

        // Appel du endpoint
        MvcResult result = mockMvc.perform(patch("/articles/update/meta")
                        .header("Authorization", "Bearer " + this.tokenUserOne)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(articleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("L'article a été mis à jour avec succès"))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data").isNumber())
                .andDo(print())
                .andReturn();
    }

    @Test
    @Order(25)
    @DisplayName("PATCH /articles/update/meta - Forbidden")
    @Transactional
    void shouldReturnForbiddenWhenUpdatingArticleMeta() throws Exception {
        // Récupérer un article pour le test
        Article article = this.articleRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Aucun article trouvé dans la base de données"));

        // Mapping vers l'objet DTO
        ArticleDto articleDto = new ModelMapper().map(article, ArticleDto.class);

        // Modifier l'ID utilisateur pour simuler une mise à jour non autorisée
        articleDto.setIdUser(this.idUserTwo);

        // Définir des modifications de métadonnées
        articleDto.setVue(1);
        articleDto.setVisibale(true);

        // Appel du endpoint avec le token du deuxième utilisateur
        mockMvc.perform(patch("/articles/update/meta")
                        .header("Authorization", "Bearer " + this.tokenUserTwo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(articleDto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    /**
     * Permet la déconnexion de l'utilisateur à la fin du test d'intégration.
     *
     * @param token String
     */
    private void logout(String token) {
        if (token != null) {
            try {
                // Preparation de la requête pour la déconnexion
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<String> request = new HttpEntity<>(headers);

                // Envoi de la requête
                ResponseEntity<String> response = new RestTemplate().exchange(
                        this.keycloakURL,
                        HttpMethod.POST,
                        request,
                        String.class);

                log.info("déconnexion {}", response.getStatusCode());

                // Accepter à la fois 200 OK et 302 FOUND comme succès
                assertTrue(
                        response.getStatusCode() == HttpStatus.OK ||
                                response.getStatusCode() == HttpStatus.FOUND,
                        "La déconnexion a échoué"
                );

            } catch (Exception e) {
                log.error("Erreur lors de la déconnexion", e);
                fail("La déconnexion a échoué avec une exception : " + e.getMessage());
            }
        }

    }


    /**
     * Méthode utilitaire pour obtenir un token JWT valide.
     */
    private String getAccessToken(String username, String password) throws Exception {
        // Appel à Keycloak pour obtenir un token
        URL url = new URL(String.format("%s/realms/ghoverblog/protocol/openid-connect/token", keycloakURL));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configuration de la requête POST
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Corps de la requête
        String body = String.format(
                "grant_type=password&client_id=%s&username=%s&password=%s",
                keycloakResource,
                username,
                password
        );
        connection.getOutputStream().write(body.getBytes());

        // Lecture de la réponse
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to fetch access token: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        // Extraction du token depuis la réponse JSON
        String jsonResponse = response.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(jsonResponse).get("access_token").asText();
    }

    /**
     * Décoder le token JWT et retourne l'id user
     *
     * @param token le JWT Token
     * @return l'id user
     */
    private String getIdUserAccessToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("sub").asString();
    }

}
