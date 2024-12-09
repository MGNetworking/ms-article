package ArticleWebService.web.endToEnd;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.entities.Article;

import ArticleWebService.entities.ArticleSave;
import ArticleWebService.entities.ArticleUpdate;
import ArticleWebService.entities.Section;
import ArticleWebService.web.ControllerArticle;
import ArticleWebService.web.SystemPropertiesActiveProfileResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.*;


import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
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
public class ControllerArticleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${keycloak.auth-server-url}")
    private String keycloakURL;

    @Value("${keycloak.resource}")
    private String keycloakResource;

    private ObjectMapper mapper = new ObjectMapper(); // permet la sérialisation et la désérialisation JSON en Java
    private String token;
    private String tokenForbidden;
    private ArticleSave articleSave;
    private ArticleUpdate articleUpdate;

    private Integer articleId;
    private Timestamp dateCreation;

    /**
     * Cette méthode s'exécute avant tous les tests pour initialiser les tokens et l'article
     */
    @BeforeAll
    void setupToken() throws Exception {

/*        System.out.println("Keycloak URL from Environment: " + environment.getProperty("keycloak.auth-server-url"));
        System.out.println("Keycloak Resource from Environment: " + environment.getProperty("keycloak.resource"));*/



        // Obtenez un token valide pour les utilisateurs de test
        token = this.getAccessToken("max", "aAA5MbezUxN5V3BHVLH4");
        tokenForbidden = this.getAccessToken("maximus", "jao81Qt89oRva2jBoa5o");

        // Préparation des données d'entrée
        articleSave = new ArticleSave(
                null,
                "user123",
                new Section(1, "Java"),
                "Nouvel Article",
                "image.png",
                "Ceci est une description",
                true,
                "Ceci est le contenu de l'article",
                Arrays.asList("source1", "source2")
        );

    }


    /**
     * Test le retour avec status 200 et vérifier qu'il n'est pas vide.
     */
    @Test
    @DisplayName("Get Article by Id ")
    public void getArticleById() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/{id}", 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // get Value JSON to string and mapping Article class object
        String responseJson = result.getResponse().getContentAsString();
        Article articles = this.mapper.readValue(responseJson, Article.class);

        assertNotNull(articles);

    }

    /**
     * Test l'échec de la recherche et attend une exception de type ArticleNotFoundException
     */
    @Test
    @DisplayName("Get Article by Id not found")
    public void getArticle_ById_NotFound() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/{id}", 5000))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> {
                            Throwable exception = result.getResolvedException();
                            Assertions.assertTrue(exception instanceof ArticleException);
                            Assertions.assertEquals(HttpStatus.NOT_FOUND, ((ArticleException) exception).getStatus());
                        }
                );
    }


    /**
     * Le test est accès sur la recherche infructueuse, mais qui renvoi une pagination.
     * Cette pagination doit ne contenir aucun article, mais avoir la structure de pagination avec les données.
     * <p>
     * Doit renvoyer une response 200 avec une pagination sans article.
     */
    @Test
    @DisplayName("Get All Article with Pagination IsEmpty")
    public void getAllArticles_IsEmpty() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticles")
                        .param("page", Integer.toString(99))
                        .param("size", Integer.toString(66))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content")
                        .isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageNumber")
                        .value(99))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageSize")
                        .value(66));

    }

    @Test
    @DisplayName("Get All Article with section and Pagination")
    public void getArticleSection() throws Exception {

        int page = 0;
        int size = 6;
        int sectionId = 1;

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticlesSection")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sectionId", String.valueOf(sectionId)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers
                        .status().isOk());
    }

    @Test
    @DisplayName("Get All Domain")
    public void getAllDomain() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllDomain"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // END POINT => /article/saveArticle

    /**
     * La création d'un article via un utilisateur authorisé
     */
    @Test
    @Order(1)
    @DisplayName("should Create Article When save Article")
    void shouldCreateArticleWhenValidRequest() throws Exception {

        // Exécuter l'appel à l'API et capturer la réponse
        MvcResult mvcResult = mockMvc.perform(post("/article/saveArticle")
                        .header("Authorization", "Bearer " + token) // Ajout du token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleSave)))
                .andExpect(status().isCreated()) // Vérifier le statut HTTP
                .andDo(print()) // Affiche la réponse dans la console
                .andReturn(); // Récupère le résultat

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

    /**
     * La création d'un article via un utilisateur non authorisé.
     */
    @Test
    @DisplayName("should Return Forbidden(403) When save Article")
    void shouldReturnForbiddenWhenUpdatingArticle() throws Exception {

        // Appel le end point
        mockMvc.perform(post("/article/saveArticle")
                        .header("Authorization", "Bearer " + tokenForbidden)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleSave)))
                .andExpect(status().isForbidden())
                .andDo(print()); // Affiche la réponse dans la console
    }

    /**
     * Test un utilisateur non authorisé.
     */
    @Test
    @DisplayName("should Return Unauthorized(401) When save Article")
    void shouldReturnUnauthorizedWhenUpdatingArticle() throws Exception {

        // Appel le end point
        mockMvc.perform(post("/article/saveArticle")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleSave)))
                .andExpect(status().isUnauthorized())
                .andDo(print()); // Affiche la réponse dans la console
    }

    // END POINT => /article/updateArticle

    @Test
    @Order(2)
    @DisplayName("should Return Create(201) When Updating Article")
    void shouldReturnCreateWhenUpdatingArticle() throws Exception {

        articleUpdate = new ArticleUpdate(
                this.articleId,
                "user123",
                new Section(1, "Python"),
                "Update Article",
                "image.png",
                "description de l'image",
                "Ceci est une description",
                "Ceci est le contenu de l'article",
                this.dateCreation,
                true,
                Arrays.asList("source1", "source2")
        );

        // Appel le end point
        mockMvc.perform(put("/article/updateArticle")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleUpdate)))
                .andExpect(status().isCreated())
                .andDo(print()); // Affiche la réponse dans la console
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
}
