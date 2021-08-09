package ghoverblog.ovh.blogarticle.configuration;

import ghoverblog.ovh.blogarticle.entities.Article;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;


@Configuration
public class PersistanceConfiguration implements RepositoryRestConfigurer {

    /**
     * Cette configuration permet d'exposer l'id de l'objet article.
     *
     * @param config
     * @param cors
     */
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(Article.class);
    }
}
