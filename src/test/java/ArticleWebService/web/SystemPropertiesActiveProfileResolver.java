package ArticleWebService.web;

import lombok.extern.slf4j.Slf4j;
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

            if (activeProfile != null && !activeProfile.isEmpty()) {
                log.info("------------------------");
                log.info("Profil actif : {}", activeProfile);
                log.info("------------------------");
                return new String[]{activeProfile};
            }

            log.info("------------------------");
            log.warn("Aucun profil trouvé dans les propriétés système. Utilisation du profil par défaut.");
            log.info("------------------------");
            return this.defaultActiveProfilesResolver.resolve(testClass);
        }
}
