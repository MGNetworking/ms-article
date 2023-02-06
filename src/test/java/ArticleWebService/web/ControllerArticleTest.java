package ArticleWebService.web;

import ArticleWebService.configuration.WebConfiguration;
import ArticleWebService.entities.ArticleForm;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
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

    private static ObjectMapper mapper = new ObjectMapper();

    @Value("${file.domain-dir}")
    private String ipLocation;

    @Value("${file.upload-dir}")
    private String directory;

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
