package ArticleWebService.web;

import ArticleWebService.configuration.WebConfiguration;
import ArticleWebService.entities.ArticleForm;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.JsonPath;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Le service de configuration doit être en cours d'exécution
 * afin d'obtenir les properties nécessaires au fonctionnement du service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "dev", resolver = SystemPropertiesActiveProfileResolver.class)
@Slf4j
public class ControllerArticleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private FileSystemStorageServiceImplementation fsssi;

    @Value("${file.domain-dir}")
    private String ipLocation;
    @Value("${file.upload-dir}")
    private String directory;
    @Value("${keycloak.auth-server-url}")
    private static String urlKeycloak;
    @Value("${keycloak.realm}")
    private static String realmKeycloak;
    @Value("${keycloak.resource}")
    private static String clientKeycloak;
    @Value("${keycloak.userTeste}")
    private static String userKeycloak;
    @Value("${keycloak.password}")
    private static String passwordKeycloak;

    private static String protocol = "/protocol/openid-connect/token";
    private static String accesToken;
    private static ObjectMapper mapper = new ObjectMapper();



    @BeforeAll
    public static void setUp() throws Exception {
        accesToken = getAccesToken();
    }

    /**
     * Permet de créer un token avant les testes d'intégration.
     *
     * @return
     * @throws Exception
     */
    private static String getAccesToken() throws Exception {
        // url vers keycloak
        String tokenUrl = "http://192.168.38.128:8888/auth" + "/realms/" + "ghoverblog" + protocol;
        // créationd e l'entéte du type application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // création d'une map de données pour l'accès a keycloak
        MultiValueMap<String, String> mapToAcces = new LinkedMultiValueMap<>();
        mapToAcces.add("client_id","overblog_angular");
        mapToAcces.add("username", "max");
        mapToAcces.add("password", "aAA5MbezUxN5V3BHVLH4");
        mapToAcces.add("grant_type", "password");

        // création de la request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapToAcces, headers);

        // envoi vers keycloak
        ResponseEntity<String> response = new RestTemplate()
                .postForEntity(tokenUrl, request, String.class);

        // vérification du status
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Echec de l'obtention du token ");
        }

        String responseBody = response.getBody();
        JsonNode jsonNode = mapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();

    }

    /**
     * @Test
     * @DisplayName("Get Article by Id ")
     * public void endPointGetArticle() throws Exception {
     * <p>
     * mockMvc.perform(MockMvcRequestBuilders
     * .get("/article/getArticle/16"))
     * .andExpect(status().isOk())
     * .andExpect(jsonPath("$.titre", containsString("L'IA")));
     * }
     * @Test
     * @DisplayName("Get All Article with Pagination")
     * public void endPointGetAllArticles() throws Exception {
     * <p>
     * mockMvc.perform(MockMvcRequestBuilders
     * .get("/article/getAllArticles?page=0&size=6"))
     * .andExpect(status().isOk())
     * .andExpect(jsonPath("$.content[0].articleId", is(10)))
     * .andExpect(jsonPath("$.content[0].titre", containsString("Fini la formation")));
     * <p>
     * }
     **/

    @Test
    @DisplayName("End point Post : save article in dataBase ")
    public void endSaveArticle() throws Exception {

        ArticleForm articleForm = new ArticleForm();
        articleForm.setIdUser(1);
        articleForm.setIdSection(1);
        articleForm.setTitre("Titre de testing");
        articleForm.setArticle("Contenu de l'article");
        articleForm.setDescription("description de l'article");
        articleForm.setVisibiliter(false);

        // mapping de l'objet au format Json
        String paylaod = mapper.writeValueAsString(articleForm);

        mockMvc.perform(post("/article/saveArticle")
                        .header("Authorization", "bearer " + this.accesToken)
                        .content(paylaod)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUser").value(1l));
    }

    @Test
    @DisplayName("End point Post : upload images in server")
    public void endPointSaveImage() throws Exception {

        ClassLoader cl = getClass().getClassLoader();

        // prendre une images dans les asset
        File filesImg = new File(cl.getResource("static/images-MockMVC/1.jpg").getFile());

        if (filesImg.exists()) {

            log.info("l'images existe : " + filesImg.getName());

            // Lecture du fichier bytes a byte
            byte[] imageByte = Files.readAllBytes(filesImg.toPath());

            // création de l'objet multipartFile
            MockMultipartFile mockMultipartFile =
                    new MockMultipartFile(
                            "images",                            // le nom du fichier
                            filesImg.getName(),                        // le nom original du fichier
                            String.valueOf(MediaType.IMAGE_JPEG),      // le type de ficher
                            imageByte);                                // le byte code de l'image

            // appel du point de terminaison
            mockMvc.perform(multipart("/article/saveImages").file(mockMultipartFile))
                    .andExpect(status().isCreated());

        } else {

            throw new Exception("L'images de test n'est pas présent dans les asset");
        }


    }


    // TODO test end point removeImages

    @Test
    @DisplayName("End point Delete : remove image in server")
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

        // prendre une images dans les asset
        //File filesImg = new File(getClass().getClassLoader().getResource("static/images-MockMVC/1.jpg").getFile());

        if (nameFile != null) {

            mockMvc.perform(MockMvcRequestBuilders.delete("/article/deleteImages")
                            .param("nameImages", nameFile))
                    .andExpect(status().isOk());

        } else {
            String message = "L'images de test n'est pas présent dans les asset";
            log.error(message);
            throw new Exception(message);
        }

    }


}
