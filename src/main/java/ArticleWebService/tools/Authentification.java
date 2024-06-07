package ArticleWebService.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.security.oauth2.jwt.Jwt;

@Component
@Slf4j
public class Authentification {

    public Authentification() {

    }

    /**
     * Permet de comparait l'id utilisateur passé en paramêtre avec l'id
     * utilisteur contenu dans le token présent dans le requête.
     *
     * @param userId l'identifiant utilisateur
     * @return un boolean qui données une authorization d'accès.
     */
    public boolean isAuthorization(String userId) {

        boolean identity = false;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        log.info("Username : " + authentication.getName());
        log.info("Detail : " + authentication.getDetails());
        log.info("Principal : " + authentication.getPrincipal());
        log.info("Authorities : " + authentication.getAuthorities());

        log.info("User id  : " + userId);


        String userIdFromToken = null;

        // Extraction de l'IO utilisateur du token
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            userIdFromToken = jwt.getClaimAsString("sub");
            log.info("instance jwt is  : " + userIdFromToken);
        } else {
            userIdFromToken = authentication.getPrincipal().toString();
            log.info("instance is not jwt : " + userIdFromToken);
        }

        // Vérification de l'identité utilisateur
        if (userId.equals(userIdFromToken)) {
            log.info("User créateur : Accès autorisé ...");
            return true;
        }

        log.info("User créateur : Accès refusé ...");
        return false;
    }
}
