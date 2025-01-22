package ArticleWebService.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

/**
 * Permet la résolution du profil actif au lancement de l'application.
 */
@Slf4j
public class SystemPropertiesActiveProfileResolver implements ActiveProfilesResolver {

    private final DefaultActiveProfilesResolver defaultActiveProfilesResolver = new DefaultActiveProfilesResolver();

        @Override
        public String[] resolve(Class<?> testClass) {
            String activeProfile = System.getProperty("spring.profiles.active");

/*            if (activeProfile != null && !activeProfile.isEmpty()) {
                log.info("------------------------");
                log.info("Profil actif : {}", activeProfile);
                log.info("------------------------");
                return new String[]{activeProfile};
            }*/

            // Récupérer les profils définis dans application.properties
            StandardEnvironment environment = new StandardEnvironment();
            String profileFromProperties = environment.getProperty("spring.profiles.active");

            if (profileFromProperties != null && !profileFromProperties.isEmpty()) {
                log.info("------------------------");
                log.info("Profil actif trouvé dans application.properties : {}", profileFromProperties);
                log.info("------------------------");
                return new String[]{profileFromProperties};
            }

            // Profil par défaut
            log.info("------------------------");
            log.warn("Aucun profil actif trouvé. Utilisation du profil par défaut.");
            log.info("------------------------");
            return this.defaultActiveProfilesResolver.resolve(testClass);
        }
}
