package ArticleWebService.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("All End point of microservice Article")
public class ControllerArticleTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Get Article by Id ")
    public void testGetArticle() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/getArticle/16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", containsString("L'IA")));
    }

    @Test
    @DisplayName("Get All Article with Pagination")
    public void testGetAllArticles() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/getAllArticles?page=0&size=6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articleId", is(10)))
                .andExpect(jsonPath("$.content[0].titre", containsString("Fini la formation")));

    }

}
