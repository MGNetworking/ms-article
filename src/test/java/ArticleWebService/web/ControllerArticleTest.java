package ArticleWebService.web;

import ArticleWebService.Exception.ArticleException;
import ArticleWebService.entities.Article;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;


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
    private Environment environment;


    private String nameImages;
    private String idArticleForDelete;

    // permet la sérialisation et la désérialisation JSON en Java
    private ObjectMapper mapper = new ObjectMapper();



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
}
