package ArticleWebService.web.endtoend;

import ArticleWebService.handler.Exception.ArticleException;
import ArticleWebService.entities.Article;

import ArticleWebService.dto.ArticleDtoSave;
import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.Section;
import ArticleWebService.handler.response.GenericApiResponse;
import ArticleWebService.service.ArticleService;
import ArticleWebService.web.SystemPropertiesActiveProfileResolver;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test d'intégration
 * Cette Classe test le control Article.
 * <p>
 * NB: Le service de configuration doit être en cours d'exécution
 * afin d'obtenir les properties nécessaires au fonctionnement du service.
 * Le Service Keycloak et la base de données associé doivent être en cours
 * d'exécution
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(resolver = SystemPropertiesActiveProfileResolver.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class ControllerArticleE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleService articleService;

    @Value("${keycloak.auth-server-url}")
    private String keycloakURL;

    @Value("${keycloak.resource}")
    private String keycloakResource;

    // permet la sérialisation / désérialisation JSON en Java
    private final ObjectMapper mapper = new ObjectMapper();
    private String tokenMax;
    private String tokenMaximus;

    private String userMax;
    private String userMaximus;

    private ArticleDtoSave articleDtoSave;
    private ArticleDtoUpdate articleDtoUpdate;

    private Integer articleId;
    private Timestamp dateCreation;

    @BeforeAll
    void setupToken() throws Exception {

        // Obtenez un token valide pour les utilisateurs de test
        tokenMax = this.getAccessToken("max", "aAA5MbezUxN5V3BHVLH4");
        tokenMaximus = this.getAccessToken("maximus", "jao81Qt89oRva2jBoa5o");

        // récupérer l'ID utilisateur
        this.userMax = getIdUserAccessToken(tokenMax);
        this.userMaximus = getIdUserAccessToken(tokenMaximus);

        // Préparation des données d'entrée
        articleDtoSave = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                this.userMax,
                "Nouvel Article",
                "image.png",
                "Ceci est une description",
                "description de l'article",
                "Ceci est le contenu de l'article",
                true
        );

    }

    @Test
    @Order(1)
    @DisplayName("GET /article/getArticle/{id} - Success")
    public void getArticleById() throws Exception {

        int idArticle = 1;

        MvcResult result = this.mockMvc.perform(get("/article/getArticle/{id}", idArticle)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andReturn();


        String responseJson = result.getResponse().getContentAsString();
        JsonNode jsonNode = this.mapper.readTree(responseJson);
        int status = result.getResponse().getStatus();

        assertEquals(200, status, "Le statut HTTP attendu est incorrect.");
        assertTrue(jsonNode.has("timestamp"), "Le champ 'timestamp' devrait exister dans la réponse");

        // Mapping de la réponse pour des vérifications supplémentaires
        GenericApiResponse<Article> genericApiResponse = this.mapper
                .readValue(responseJson, new TypeReference<GenericApiResponse<Article>>() {
                });

        // Vérifiez que l'ApiResponse et l'Article ne sont pas null
        assertNotNull(genericApiResponse, "La réponse API ne doit pas être null");
        assertNotNull(genericApiResponse.getData(), "Les données de l'article ne doivent pas être null");

    }

    @Test
    @Order(2)
    @DisplayName("GET /article/getArticle/{id} - ID négatif")
    public void getArticleById_BadRequest() throws Exception {
        this.mockMvc.perform(get("/article/getArticle/{id}", -1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.path")
                        .value("/article/getArticle/-1"));
    }


    @Test
    @Order(3)
    @DisplayName("GET /article/getArticle/{id} - Invalid ID - MethodArgumentTypeMismatchException")
    public void testValidationErrors() throws Exception {
        this.mockMvc.perform(get("/article/getArticle/{id}", "invalid-id")
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
    @DisplayName("GET /article/getAllArticles - ID manquant")
    public void getArticleById_NullId() throws Exception {
        this.mockMvc.perform(get("/article/getArticle/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound()); // Vérifie le statut 404

    }

    @Test
    @Order(5)
    @DisplayName("GET /article/getAllArticles - Article Not found - ArticleException")
    public void getArticle_ById_NotFound() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/{id}", 5000))
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
    @DisplayName("GET /article/getAllArticles - Recherche reussi une pagination d'article status 200")
    public void getAllArticles_pageble_OK() throws Exception {

        int page = 0, size = 10;

        MvcResult result = this.mockMvc.perform(get("/article/getAllArticles")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
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
        assertTrue(jsonContext.read("$.timestamp") != null, "Le champ 'timestamp' est manquant.");

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
    @DisplayName("GET /article/getAllArticles - Success")
    public void getAllArticles_pageble_IsEmpty() throws Exception {

        int page = 99, size = 66;

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticles")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message")
                        .value(String.format("La page %d et le nombre d'éléments %d", page, size)))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/article/getAllArticles"))
                .andExpect(jsonPath("$.data.content").isArray()) // Vérifie que 'content' est un tableau
                .andExpect(jsonPath("$.data.content").isEmpty()) // Vérifie que 'content' est vide
                .andExpect(jsonPath("$.data.pageable").exists()) // Vérifie que 'pagination' existe
                .andDo(print());
    }

    @Test
    @Order(8)
    @DisplayName("GET /article/getAllArticlesSection - Success")
    public void getArticleSection_pageble_OK() throws Exception {

        int page = 0, size = 6, section = 1;
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticlesSection")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sectionId", String.valueOf(section)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Page %d nombre d'élement %d", page, size)))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/article/getAllArticlesSection"))
                .andExpect(jsonPath("$.data.content").isArray()) // Vérifie que 'content' est un tableau
                .andExpect(jsonPath("$.data.content").isNotEmpty()) // Vérifie que 'content' est vide
                .andExpect(jsonPath("$.data.pageable").exists()) // Vérifie que 'pagination' existe
                .andDo(print());

    }

    @Test
    @Order(9)
    @DisplayName("GET /article/getAllArticlesSection - Success")
    public void getArticleSection_pageble_IsEmpty() throws Exception {

        int page = 99, size = 66, section = 1;
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticlesSection")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sectionId", String.valueOf(section)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Page %d nombre d'élement %d", page, size)))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/article/getAllArticlesSection"))
                .andExpect(jsonPath("$.data.content").isArray()) // Vérifie que 'content' est un tableau
                .andExpect(jsonPath("$.data.content").isEmpty()) // Vérifie que 'content' est vide
                .andExpect(jsonPath("$.data.pageable").exists()) // Vérifie que 'pagination' existe
                .andDo(print());

    }

    @Test
    @Order(10)
    @DisplayName("GET /article/getAllDomain - Success")
    public void getAllDomain() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllDomain"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("POST /article/saveArticle - Success")
    void shouldCreateArticleWhenValidRequest() throws Exception {

        // Exécuter l'appel à l'API et capturer la réponse
        MvcResult mvcResult = this.mockMvc.perform(post("/article/saveArticle")
                        .header("Authorization", "Bearer " + tokenMax) // Ajout du token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleDtoSave)))
                .andExpect(status().isCreated()) // Vérifier le statut HTTP
                .andDo(print()) // Affiche la réponse dans la console
                .andReturn(); // Récupère le résultat

        System.out.println("jsonResponse : " + mvcResult.getResponse().getContentAsString());
        // Extraire le contenu de la réponse
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        // Convertir la réponse en objet ou récupérer l'ID directement
        JsonNode responseJson = objectMapper.readTree(jsonResponse);
        this.articleId = responseJson.get("data").get("idArticle").asInt();
        String date = responseJson.get("data").get("dateCreation").asText();
        this.dateCreation = Timestamp.valueOf(date);

        // Assertion pour vérifier que l'ID est correct
        assertNotNull(articleId, "L'ID de l'article ne doit pas être null");
        System.out.println("Article créé avec ID : " + this.articleId);
        System.out.println("Date de création : " + this.dateCreation);
    }

    @Test
    @Order(12)
    @DisplayName("POST /article/saveArticle - Forbidden")
    void shouldReturnForbiddenWhenUpdatingArticle() throws Exception {

        mockMvc.perform(post("/article/saveArticle")
                        .header("Authorization", "Bearer " + tokenMaximus)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleDtoSave)))
                .andExpect(status().isForbidden())
                .andDo(print()); // Affiche la réponse dans la console
    }

    @Test
    @Order(13)
    @DisplayName("POST /article/saveArticle - Unauthoriezed")
    void shouldReturnUnauthorizedWhenUpdatingArticle() throws Exception {

        mockMvc.perform(post("/article/saveArticle")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleDtoSave)))
                .andExpect(status().isUnauthorized())
                .andDo(print()); // Affiche la réponse dans la console
    }

    @Test
    @Order(14)
    @DisplayName("PUT /article/updateArticle - Is created")
    void shouldReturnCreateWhenUpdatingArticle() throws Exception {

        this.articleDtoUpdate = new ArticleDtoUpdate(
                this.articleId,
                this.userMax,
                new Section(1, "Java"),
                "Titre Article Update test",
                "https://image.png",
                "description de l'image",
                "Ceci est une description d'article",
                "Ceci est le contenu de l'article",
                true,
                1,
                this.dateCreation,
                null
        );

        // Appel le end point
        mockMvc.perform(put("/article/updateArticle")
                        .header("Authorization", "Bearer " + tokenMax)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(this.articleDtoUpdate)))
                .andExpect(status().isCreated())
                .andDo(print()); // Affiche la réponse dans la console
    }

    @Test
    @Order(15)
    @DisplayName("DELETE /article/deleteArticle/{idArticle}/{idUser} - Is Forbidden")
    void shouldReturnFalseAfterDeleteArticle() throws Exception {

        this.mockMvc.perform(delete("/article/deleteArticle/{idArticle}/{idUser}",
                        this.articleId, this.userMax)
                        .header("Authorization", "Bearer " + tokenMaximus)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit"))
                .andDo(print());
    }

    @Test
    @Order(16)
    @DisplayName("DELETE /article/deleteArticle/{idArticle}/{idUser} - Success")
    void shouldReturnTrueAfterDeleteArticle() throws Exception {
        this.mockMvc.perform(delete("/article/deleteArticle/{idArticle}/{idUser}"
                        , this.articleId, this.userMaximus)
                        .header("Authorization", "Bearer " + this.tokenMax)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("La suppression de votre article a été réaliser avec succès"))
                .andDo(print());
    }


    /**
     * Permet la déconnexion de l'utilisateur à la fin du test d'intégration.
     *
     * @param token String
     */
    private void logout(String token) {

        if (token != null) {

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
            // Le status 200 indique que la déconnexion a réussi
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

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

    private String getIdUserAccessToken(String token) {

        // Décoder le token JWT
        DecodedJWT decodedJWT = JWT.decode(token);

        // Récupérer des valeurs (claims) du token
        String subject = decodedJWT.getSubject(); // Récupère le "sub" claim
        Claim subClaim = decodedJWT.getClaim("sub"); // Récupère l'id user
        String idUser = subClaim.asString(); // Convertir le claim en String

        // Afficher les valeurs
        System.out.println("Subject: " + subject);
        System.out.println("idUser: " + idUser);

        return idUser;
    }

}
