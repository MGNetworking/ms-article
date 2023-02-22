package ArticleWebService.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

import java.util.regex.Pattern;

/**
 * Permet la résolution du profile actif au lancement de l'application.
 */
@Slf4j
public class SystemPropertiesActiveProfileResolver implements ActiveProfilesResolver {

    private final DefaultActiveProfilesResolver defaultActiveProfilesResolver = new DefaultActiveProfilesResolver();

    @Override
    public String[] resolve(Class<?> testClass) {

        String[] springPropertieProfile = new String[1];

        log.info("Les listes et recherche des propriétées d'environement Java du projet ms-article");
        System.getProperties().values().forEach(
                ele -> {
                    log.info(ele.toString());
                    if (Pattern.matches("-Dspring.profiles.active=dev", ele.toString())) {
                        log.info("*****************************");
                        log.info("le profile dev a était trouver ");
                        log.info("*****************************");
                        springPropertieProfile[0] = "dev";

                    } else if (Pattern.matches("-Dspring.profiles.active=pre-prod", ele.toString())) {

                        log.info("*****************************");
                        log.info("le profile pre-prod a était trouver ");
                        log.info("*****************************");

                        springPropertieProfile[0] = "pre-prod";

                    } else if (Pattern.matches("-Dspring.profiles.active=prod", ele.toString())) {

                        log.info("*****************************");
                        log.info("le profile prod a était trouver ");
                        log.info("*****************************");

                        springPropertieProfile[0] = "prod";

                    }
                }
        );

        if (springPropertieProfile[0] == null) {

            log.error("*****************************");
            log.error("Le profile n'a pas était trouver dans les variables d'environnement ");
            log.error("C'est donc le profile (dev) présent dans les tests d'intégration qui sera activé");
            log.error("*****************************");

            return this.defaultActiveProfilesResolver.resolve(testClass);
        } else {

            return springPropertieProfile;
        }

    }


}
