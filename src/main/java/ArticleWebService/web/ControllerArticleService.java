package ArticleWebService.web;

import ArticleWebService.component.ConfigurationArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
@RequestMapping("/article-config")
public class ControllerArticleService {

    @Autowired
    ConfigurationArticle configurationArticle;

    @GetMapping(value = "/config-service")
    public ConfigurationArticle getConfigurationService(){
        return configurationArticle;
    }
}
