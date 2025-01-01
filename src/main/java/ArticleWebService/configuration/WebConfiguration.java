package ArticleWebService.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Cette classe est une classe de configuration sur la partie static du projet
 */
@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport {

    /**
     * Permet d'atteindre les élèvements static surtout utiliser pour les phases de teste via les Mock MVC
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
