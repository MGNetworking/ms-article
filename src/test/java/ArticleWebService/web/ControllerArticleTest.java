package ArticleWebService.web;

import ArticleWebService.Exception.ArticleNotFoundException;
import ArticleWebService.configuration.WebConfiguration;
import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
import ArticleWebService.entities.ArticleForm;
import ArticleWebService.entities.Section;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.keycloak.KeycloakSecurityContext;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.web.JsonPath;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.ws.rs.PUT;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

/**
 * Le service de configuration doit être en cours d'exécution
 * afin d'obtenir les properties nécessaires au fonctionnement du service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(value = "dev", resolver = SystemPropertiesActiveProfileResolver.class)
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
    @Value("${file.upload-dir}")
    private String directory;

    @Value("${urlToken.keycloak}")
    private String urlTokenKeycloak;
    @Value("${urlLogout.keycloak}")
    private static String urlLogoutKeycloak;
    @Value("${keycloak.resource}")
    private String clientKeycloak;
    @Value("${user.keycloak}")
    private String userKeycloak;
    @Value("${password-user.keycloak}")
    private String passwordKeycloak;

    private static String accesToken;
    // permet la sérialisation et de désérialisation JSON en Java
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUpAccesToken() throws Exception {

        // Certifie que les variables d'environement
        // keycloak sont initialisées avant l'initialisation du token d'accès
        Assertions.assertNotNull(this.urlTokenKeycloak);
        Assertions.assertNotNull(this.clientKeycloak);
        Assertions.assertNotNull(this.userKeycloak);
        Assertions.assertNotNull(this.passwordKeycloak);

        // initialisation des variable statis
        urlLogoutKeycloak = environment.getProperty("urlLogout.keycloak");

        Assertions.assertNotNull(urlLogoutKeycloak);

        // si pas initialiser
        if (accesToken == null) {
            accesToken = getAccesToken();
        }

    }


    /**
     * permet la déconnexion de l'utilisateur à la fin du test d'integration .
     *
     * @throws Exception dans le cas d'une echec de connexion.
     */
    @AfterAll
    public static void setlogout() throws Exception {

        // teste value acces Token
        if (accesToken != null) {
            // Preparation de la requête pour la déconnection
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accesToken);
            HttpEntity<String> request = new HttpEntity<>(headers);

            // Envoi de la requête
            ResponseEntity<String> response = new RestTemplate().exchange(
                    urlLogoutKeycloak,
                    HttpMethod.POST,
                    request,
                    String.class);

            // status 200 indique que la déconnexion a réussi
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

            accesToken = null;
        }
    }

    /**
     * Permet d'initialiser un token pour les testes d'accès
     *
     * @return String access_token
     * @throws Exception en cas d'un status != 200
     */
    private String getAccesToken() throws Exception {

        // création de l'entéte du type application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // création d'une map de données pour l'accès a keycloak
        MultiValueMap<String, String> mapToAcces = new LinkedMultiValueMap<>();
        mapToAcces.add("client_id", this.clientKeycloak);
        mapToAcces.add("username", this.userKeycloak);
        mapToAcces.add("password", this.passwordKeycloak);
        mapToAcces.add("grant_type", "password");

        // requêtes externe vers keycloak
        ResponseEntity<String> response = new RestTemplate()
                .postForEntity(this.urlTokenKeycloak,
                        new HttpEntity<>(mapToAcces, headers),
                        String.class);

        // status 200 indique que l'authentification a réussi
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);

        // mapping du token
        JsonNode jsonNode = this.mapper.readTree(response.getBody());
        return jsonNode.get("access_token").asText();

    }

/*
    public static String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey("your-signing-key")
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
*/


    /**
     * Test le retour avec tatus 200 et vérifier qu'il n'est pas  vide
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Get Article by Id ")
    public void endPointGetArticleById() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/{id}", 1))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // get Value JSON to string and mapping Article class object
        String responseJson = result.getResponse().getContentAsString();
        Article articles = this.mapper.readValue(responseJson, Article.class);

        Assertions.assertNotNull(articles);

    }

    /**
     * Test l'échec de la recherche et attent une exception de type ArticleNotFoundException
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Get Article by Id not found")
    public void endPointGetArticle_NotFound() throws Exception {

        int id = 5000;

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/{id}", id))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> Assertions.assertTrue(
                        result.getResolvedException() instanceof ArticleNotFoundException));

    }


    /**
     * Le test est accès sur la recherche infructeuse mais qui renvoi une pagination .
     * Cette pagination doit ne contenir aucun article mais avoir la structure de pagination avec les ces données.
     * <p>
     * Doit renvoyer une reponse 200 avec une pagination sans article .
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Get All Article with Pagination IsEmpty")
    public void endPointGetAllArticles_IsEmpty() throws Exception {

        Integer page = 99;
        Integer size = 66;

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticles")
                        .param("page", Integer.toString(page))
                        .param("size", Integer.toString(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content")
                        .isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageNumber")
                        .value(page))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageSize")
                        .value(size))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements")
                        .value(0));

    }

    @Test
    @DisplayName("Get All Article with section and Pagination")
    public void endPointGetArticleSection() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticlesSection?page=0&size=6&sectionId=1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].titre", containsString("Titre de testing")));

    }


    @Test
    @DisplayName("Save article in dataBase ")
    public void endSaveArticle() throws Exception {

        ArticleForm articleForm = new ArticleForm();
        articleForm.setIdUser(1);

        Section section = new Section();
        section.setIdSection(1);
        articleForm.setSection(section);

        articleForm.setTitre("Titre de testing");
        articleForm.setArticle("Contenu de l'article");
        articleForm.setDescription("description de l'article");
        articleForm.setVisibiliter(false);

        // sérialisation en Json de l'articleForm
        String paylaod = mapper.writeValueAsString(articleForm);

        mockMvc.perform(MockMvcRequestBuilders.post("/article/saveArticle")
                        .header("Authorization", "bearer " + this.accesToken)
                        .content(paylaod)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idUser").value(1l));
    }

    @Test
    @DisplayName("Update article in dataBase ")
    public void endUpdateArticle() throws Exception {

        // get article 1
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Récupération de l'objet dans la réponse de la requête GET au format Json
        String objectJson = result.getResponse().getContentAsString();
        log.info("objectJson " + objectJson);

        // Désérialisation JSON vers objet Article
        Article article = this.mapper.readValue(objectJson, Article.class);
        log.info("article " + article);

        // Mapping de l'article vers ArticleForm
        ModelMapper modelMapper = new ModelMapper();
        ArticleForm articleForm = modelMapper.map(article, ArticleForm.class);

        // modification de l'article
        articleForm.setArticle("Cette article et un test de mise a jour");

        // sérialiseation du ArticleForm en JSON
        String paylaod = this.mapper.writeValueAsString(articleForm);

        // envoi les modifications de l'article
        mockMvc.perform(MockMvcRequestBuilders.post("/article/saveArticle")
                        .header("Authorization", "bearer " + this.accesToken)
                        .content(paylaod)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idUser").value(1l));
    }

    @Test
    @DisplayName("Upload images in server")
    public void endPointSaveImage() throws Exception {

        ClassLoader cl = getClass().getClassLoader();

        // prendre une images dans les asset
        File filesImg = new File(cl.getResource("static/images-MockMVC/1.jpg").getFile());

        File filesImg2 = new File(cl.getResource("2.jpg").getFile());

        // verifie l'existance de l'images dans les assets
        Assertions.assertTrue(filesImg.exists());

        Assertions.assertNotNull(filesImg2);

        log.info("l'images : " + filesImg.getName() + " est présente");

        // Lecture du fichier bytes a bytes
        byte[] imageByte = Files.readAllBytes(filesImg.toPath());

        // création de l'objet multipartFile
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile(
                        "images",                            // le nom du fichier
                        filesImg.getName(),                        // le nom original du fichier
                        String.valueOf(MediaType.IMAGE_JPEG),      // le type de ficher
                        imageByte);                                // le byte code de l'image

        // appel du point de terminaison
        mockMvc.perform(MockMvcRequestBuilders.multipart("/article/saveImages").file(mockMultipartFile))
                .andExpect(MockMvcResultMatchers.status().isCreated());


    }

    /**
     * Permet testé la suppression d'un images sur le serveur.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Remove image in server")
    public void endPointdeleteImages() throws Exception {

        File[] files = new File(this.directory).listFiles();
        log.info("nombre de fichier : " + files.length);

        String nameFile = null;
        for (File f : files) {

            log.info("nom du fichier : " + files.getClass().getName());
            Matcher matcher = Pattern.compile("([\\w]+\\.jpg)").matcher(f.getName());

            if (matcher.find()) {
                nameFile = f.getName();
                log.info("File name find : " + nameFile);
                break;
            }
        }

        // certifie que l'images est présent sur le serveur
        Assertions.assertNotNull(nameFile);

        mockMvc.perform(MockMvcRequestBuilders.delete("/article/deleteImages")
                        .param("nameImages", nameFile))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


}
