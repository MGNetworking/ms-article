package ArticleWebService;

import ArticleWebService.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BlogArticleApplication {

    @Autowired
    private ArticleService articleService;

    public static void main(String[] args) {

        SpringApplication.run(BlogArticleApplication.class, args);
    }
}
