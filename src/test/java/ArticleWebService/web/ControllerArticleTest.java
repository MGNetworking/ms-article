package ArticleWebService.web;

import ArticleWebService.configuration.WebConfiguration;
import ArticleWebService.service.FileSystemStorageServiceImplementation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

/**
    @Test
    @DisplayName("Get Article by Id ")
    public void endPointGetArticle() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getArticle/16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", containsString("L'IA")));
    }

    @Test
    @DisplayName("Get All Article with Pagination")
    public void endPointGetAllArticles() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/article/getAllArticles?page=0&size=6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articleId", is(10)))
                .andExpect(jsonPath("$.content[0].titre", containsString("Fini la formation")));

    }

 **/

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
            String message = "L'images de test n'est pas présent dans les asset";
            log.error(message);
            throw new Exception(message);
        }


    }


    // TODO test end point removeImages

    @Test
    @DisplayName("End point Delete : remove image in server")
    public void endPointdeleteImages() throws Exception {

        ClassLoader cl = getClass().getClassLoader();
        // prendre une images dans les asset
        File filesImg = new File(cl.getResource("static/images-MockMVC/1.jpg").getFile());

        if (filesImg.exists()) {

            mockMvc.perform(MockMvcRequestBuilders.delete("/article/removeImages")
                            .param("nameImages", "1.jpg"))
                    .andExpect(status().isOk());


        } else {
            String message = "L'images de test n'est pas présent dans les asset";
            log.error(message);
            throw new Exception(message);
        }

    }


}
