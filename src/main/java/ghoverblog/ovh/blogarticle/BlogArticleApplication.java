package ghoverblog.ovh.blogarticle;

import ghoverblog.ovh.blogarticle.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

import ghoverblog.ovh.blogarticle.entities.Article;
import ghoverblog.ovh.blogarticle.repository.ArticleRepository;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@EnableDiscoveryClient
public class BlogArticleApplication  {

    @Autowired
    private ArticleService articleService;

    public static void main(String[] args) {
        SpringApplication.run(BlogArticleApplication.class, args);
    }

}
