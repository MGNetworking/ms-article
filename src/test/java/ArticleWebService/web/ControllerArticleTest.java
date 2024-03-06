package ArticleWebService.web;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleSave;
import ArticleWebService.entities.ArticleUpdate;
import ArticleWebService.entities.Section;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Le service de configuration doit être en cours d'exécution
 * afin d'obtenir les properties nécessaires au fonctionnement du service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ActiveProfiles(value = "preprod", resolver = SystemPropertiesActiveProfileResolver.class)
@ActiveProfiles(resolver = SystemPropertiesActiveProfileResolver.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class ControllerArticleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private FileSystemStorageServiceImplementation fsssi;

    @Autowired
    private Environment environment;

    @Value("${file.domain-dir}")
    private String ipLocation;

    // URL connection
    @Value("${urlToken.keycloak}")
    private String urlTokenKeycloak;
    @Value("${urlLogout.keycloak}")
    private String urlLogoutKeycloak;
    @Value("${keycloak.resource}")
    private String clientKeycloak;

    // name user Authentification keycloak
    @Value("${user-Test_0.keycloak}")
    private String userTest_0Keycloak;
    // password user Authentification keycloak
    @Value("${password-user-Test_0.keycloak}")
    private String passwordTest_0Keycloak;

    @Value("${user-Test_1.keycloak}")
    private String userTest_1Keycloak;
    @Value("${password-user-Test_1.keycloak}")
    private String passwordTest_1Keycloak;

    @Value("${user-Test_2.keycloak}")
    private String userTest_2Keycloak;
    @Value("${password-user-Test_2.keycloak}")
    private String passwordTest_2Keycloak;

    // identifiant utilisateur
    // ROLE : ADMIN / USER
    @Value("${id-user-test0.keycloak}")
    private String idUserTest_0;

    //ROLE : USER
    @Value("${id-user-test1.keycloak}")
    private String idUserTest_1;

    // ROLE : (aucun role)
    @Value("${id-user-test2.keycloak}")
    private String idUserTest_2;

    // les tokens de tout les utilisateurs de tests
    private static String accesTokenTest_0;
    private static String accesTokenTest_1;
    private static String accesTokenTest_2;

    private String nameImages;
    private String idArticleForDelete;

    // permet la sérialisation et la désérialisation JSON en Java
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUpAccesToken() throws Exception {

        // Certifie que les variables environment
        // keycloak sont initialisées avant l'initialisation du token d'accès
        Assertions.assertNotNull(this.urlTokenKeycloak);
        Assertions.assertNotNull(this.clientKeycloak);

        Assertions.assertNotNull(this.userTest_0Keycloak);
        Assertions.assertNotNull(this.passwordTest_0Keycloak);

        Assertions.assertNotNull(this.userTest_1Keycloak);
        Assertions.assertNotNull(this.passwordTest_1Keycloak);

        Assertions.assertNotNull(this.userTest_2Keycloak);
        Assertions.assertNotNull(this.passwordTest_2Keycloak);

        // initialisation des variable static
        this.urlLogoutKeycloak = environment.getProperty("urlLogout.keycloak");

        Assertions.assertNotNull(this.urlLogoutKeycloak);

        // si pas initialiser
        if (accesTokenTest_0 == null) {
            accesTokenTest_0 = getAccesToken(this.userTest_0Keycloak, this.passwordTest_0Keycloak);
            Assertions.assertNotNull(accesTokenTest_0);
        }

        if (accesTokenTest_1 == null) {
            accesTokenTest_1 = getAccesToken(this.userTest_1Keycloak, this.passwordTest_1Keycloak);
            Assertions.assertNotNull(accesTokenTest_1);
        }

        if (accesTokenTest_2 == null) {
            accesTokenTest_2 = getAccesToken(this.userTest_2Keycloak, this.passwordTest_2Keycloak);
            Assertions.assertNotNull(accesTokenTest_2);
        }

    }

    /**
     * Permet la déconnexion de l'utilisateur à la fin du test d'intégration.
     *
     * @param accesToken String
     */
    private void logout(String accesToken) {

        if (accesToken != null) {

            // Preparation de la requête pour la déconnexion
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accesToken);
            HttpEntity<String> request = new HttpEntity<>(headers);

            // Envoi de la requête
            ResponseEntity<String> response = new RestTemplate().exchange(
                    this.urlLogoutKeycloak,
                    HttpMethod.POST,
                    request,
                    String.class);

            log.info("déconnection " + response.getStatusCode());
            // Le status 200 indique que la déconnexion a réussi
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        }

    }

    /**
     * Utilisé à la fin des tests d'intégration.
     *
     * @throws Exception dans le cas d'un échec de connexion.
     */
    @AfterAll
    public void setlogout() throws Exception {

        this.logout(accesTokenTest_0);
        this.logout(accesTokenTest_1);
        this.logout(accesTokenTest_2);

    }

    /**
     * Permet d'initialiser un token pour les testes d'accès
     *
     * @return String access_token
     * @throws Exception en cas d'un status != 200
     */
    private String getAccesToken(String userKeycloak, String passWordkeycloak) throws Exception {

        // création de l'entête du type application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // création d'une map de données pour l'accès a keycloak
        MultiValueMap<String, String> mapToAcces = new LinkedMultiValueMap<>();
        mapToAcces.add("client_id", this.clientKeycloak);
        mapToAcces.add("username", userKeycloak);
        mapToAcces.add("password", passWordkeycloak);
        mapToAcces.add("grant_type", "password");

        // requêtes externe vers keycloak
        ResponseEntity<String> response = new RestTemplate()
                .postForEntity(this.urlTokenKeycloak,
                        new HttpEntity<>(mapToAcces, headers),
                        String.class);

        // Le status 200 indique que l'authentification a réussi
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);

        // mapping du token
        JsonNode jsonNode = this.mapper.readTree(response.getBody());
        return jsonNode.get("access_token").asText();

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

        Assertions.assertNotNull(articles);

    }

    /**
     * Test l'échec de la recherche et attend une exception de type ArticleNotFoundException
     */
    @Test
    @DisplayName("Get Article by Id not found")
    public void getArticle_NotFound() throws Exception {

        int id = 5000;

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/{id}", id))
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

        Integer page = 99;
        Integer size = 66;

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticles")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content")
                        .isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageNumber")
                        .value(page))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageSize")
                        .value(size));

    }

    /**
     * @throws Exception
     */
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


    /**
     * n°1
     * Teste la sauvegarde d'un article dont l'utilisateur possède le ROLE ADMIN
     *
     * @throws Exception
     */
//    @Test
//    @Order(3)
//    @DisplayName("Save article in dataBase ")
//    public void saveArticle() throws Exception {
//
//        ArticleSave articleForm = new ArticleSave();
//        articleForm.setIdUser(this.idUserTest_0);
//
//        Section section = new Section();
//        section.setIdSection(1);
//        articleForm.setSection(section);
//
//        articleForm.setTitre("Titre de testing");
//        articleForm.setArticle("Contenu de l'article");
//        articleForm.setDescription("description de l'article");
//        articleForm.setVisibiliter(false);
//
//        // sérialisation en Json de l'articleForm
//        String paylaod = mapper.writeValueAsString(articleForm);
//
//        MvcResult resul = mockMvc.perform(MockMvcRequestBuilders.post("/article/saveArticle")
//                        .header("Authorization", "Bearer " + accesTokenTest_0)
//                        .content(paylaod)
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .accept(MediaType.APPLICATION_JSON_VALUE))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isCreated())
//                .andExpect(MockMvcResultMatchers
//                        .jsonPath("$.idUser")
//                        .value(this.idUserTest_0))
//                .andReturn();
//
//        String reponseContentString = resul.getResponse().getContentAsString();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonNode = objectMapper.readTree(reponseContentString);
//        this.idArticleForDelete = jsonNode.get("idArticle").asText();
//
//        Assertions.assertNotNull(this.idArticleForDelete);
//    }

    /**
     * Test la mise à jour d'article. Cet article appartient à l'utilisateur test_1
     * et la mise à jour et effectuer via l'utilisateur possèdent les droites ADMIN.
     * le user-id => a2b57a09-2e47-4690-b76f-7bc3afdbbae1
     * Correspondant a l'identifiant utilisateur max possèdant les droits ADMIN.
     *
     * @throws Exception
     */
//    @Test
//    @Order(4)
//    @DisplayName("Update article in dataBase ")
//    public void updateArticle() throws Exception {
//
//        // get article 1
//        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
//                        .get("/article/getArticle/{id}", this.idArticleForDelete))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andReturn();
//
//        // Récupération de l'objet dans la réponse de la requête GET au format Json
//        String objectJson = result.getResponse().getContentAsString();
//        log.info("objectJson " + objectJson);
//
//        // Désérialisation JSON vers objet Article
//        Article article = this.mapper.readValue(objectJson, Article.class);
//        log.info("article " + article);
//
//        // Mapping de l'article vers ArticleUpdate
//        ModelMapper modelMapper = new ModelMapper();
//        ArticleUpdate articleUpdate = modelMapper.map(article, ArticleUpdate.class);
//
//        // modification de l'article
//        if (articleUpdate.getTitre().equals("saveArticle_USER_OK")) {
//            log.info("modification du titre de l'article");
//            articleUpdate.setArticle("saveArticle_USER_OK => is ok ");
//        } else {
//            articleUpdate.setArticle("saveArticle_USER_OK");
//        }
//
//
//        // serialization du ArticleForm en JSON
//        String paylaod = this.mapper.writeValueAsString(articleUpdate);
//
//        // envoi les modifications de l'article
//        mockMvc.perform(MockMvcRequestBuilders.put("/article/updateArticle")
//                        .header("Authorization", "Bearer " + accesTokenTest_0)
//                        .header("user-id", "a2b57a09-2e47-4690-b76f-7bc3afdbbae1")
//                        .content(paylaod)
//                        .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isCreated())
//                .andExpect(MockMvcResultMatchers
//                        .jsonPath("$.data.dateMaj")
//                        .exists());
//    }


//    @Test
//    @Order(5)
//    @DisplayName("Delete article by ID")
//    public void deleteArticle() throws Exception {
//
//        this.mockMvc.perform(MockMvcRequestBuilders
//                        .delete("/article/deleteArticle/{id}", this.idArticleForDelete)
//                        .header("Authorization", "Bearer " + accesTokenTest_0)
//                        .header("user-id", "a2b57a09-2e47-4690-b76f-7bc3afdbbae1"))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isOk());
//    }

//    @Test
//    @Order(1)
//    @DisplayName("Upload images in server")
//    public void saveImage() throws Exception {
//
//        ClassLoader cl = getClass().getClassLoader();
//
//        // prendre une images dans les asset
//        File filesImg = new File(cl.getResource("static/images-MockMVC/1.jpg").getFile());
//
//        File filesImg2 = new File(cl.getResource("2.jpg").getFile());
//
//        // Vérifie l'existence de l'image dans les assets
//        Assertions.assertTrue(filesImg.exists());
//
//        Assertions.assertNotNull(filesImg2);
//
//        log.info("l'images : " + filesImg.getName() + " est présente");
//
//        // Lecture du fichier bytes a bytes
//        byte[] imageByte = Files.readAllBytes(filesImg.toPath());
//
//        // création de l'objet multipartFile
//        MockMultipartFile mockMultipartFile =
//                new MockMultipartFile(
//                        "images",                            // le nom du fichier
//                        filesImg.getName(),                        // le nom original du fichier
//                        String.valueOf(MediaType.IMAGE_JPEG),      // le type de ficher
//                        imageByte);                                // le byte code de l'image
//
//        // appel du point de terminaison
//        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/article/saveImages")
//                        .file(mockMultipartFile))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isCreated())
//                .andReturn();
//
//        String reponseString = result.getResponse().getContentAsString();
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonResponse = objectMapper.readTree(reponseString);
//        this.nameImages = jsonResponse.get("value").asText();
//
//        Assertions.assertNotNull(this.nameImages);
//    }

    /**
     * Permet testé la suppression d'une image sur le serveur.
     *
     * @throws Exception
     */
//    @Test
//    @Order(2)
//    @DisplayName("Remove image in server")
//    public void deleteImages() throws Exception {
//
//
//        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/article/deleteImages")
//                        .param("nameImages", this.nameImages))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers
//                        .status()
//                        .isOk())
//                .andReturn();
//
//        String reponseString = result.getResponse().getContentAsString();
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonResponse = objectMapper.readTree(reponseString);
//        String nameDelete = jsonResponse.get("data").asText();
//
//        Assertions.assertEquals(this.nameImages, nameDelete);
//    }


}
