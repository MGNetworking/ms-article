package ArticleWebService.security;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe de configuration pour Keycloak dans un projet Spring Boot.
 *
 * Cette classe permet d'utiliser les fichiers de configuration standard de Spring Boot
 * (comme application.yml ou bootstrap.yml) pour la configuration de Keycloak,
 * au lieu d'utiliser le fichier keycloak.json.
 *
 * Grâce à cette configuration, les propriétés Keycloak définies dans application.yml
 * sont automatiquement détectées et utilisées par Spring Security.
 *
 */
@Configuration
public class KeycloakApaterConfi {

    /**
     * Bean de résolution de configuration pour Keycloak.
     *
     * Ce bean configure Keycloak pour utiliser les propriétés définies
     * dans les fichiers application.yml ou bootstrap.yml du projet Spring Boot.
     *
     * Sans ce bean, Keycloak cherchera par défaut un fichier keycloak.json pour
     * sa configuration, ce qui n'est pas recommandé dans un projet Spring Boot.
     *
     * @return une instance de KeycloakSpringBootConfigResolver
     */
    @Bean
    public KeycloakSpringBootConfigResolver configResolver() {

        return new KeycloakSpringBootConfigResolver();
    }
}
