package ArticleWebService;

import ArticleWebService.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;


@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class Article {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(Article.class, args);
    }
}
