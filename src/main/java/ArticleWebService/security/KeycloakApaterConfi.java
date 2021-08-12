package ArticleWebService.security;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KeycloakApaterConfi {

    /**
     * Permet la gestion de configuration base sur spring boot
     * et donc va recherche sa configuration dans le fichier
     * application.properties et non keycloak.json
     *
     * @return
     */
    @Bean
    public KeycloakSpringBootConfigResolver configResolver() {
        return new KeycloakSpringBootConfigResolver();
    }
}
