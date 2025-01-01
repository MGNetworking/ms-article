package ArticleWebService.security;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
@Component
public class Access {

    public Access() {
    }

    /**
     * Permet de vérifier les droites d'accès à une ressource en fonction
     * de ses rôles ou de son nom
     * Cette méthode utilise le token Bearer transmis par la requête pour
     * examiner les droites de l'utilisateur.
     *
     * @param userId l'identifiant utilisateur
     * @return un boolean qui données une authorization d'accès.
     */
    public boolean isAuthorization(String userId) {

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        log.info("******[ Log Authorization ]**********");
        log.info("User id: {}", userId);
        log.info("Username: {}", authentication.getName());
        log.info("Detail: {}", authentication.getDetails());
        log.info("Principal: {}", authentication.getPrincipal());
        log.info("Authorities: {}", authentication.getAuthorities());

        // Test l'id user paramètre avec l'id User token
        boolean status = false;
        if (userId.equals(this.getUserIdFromToken(authentication))) {
            log.info("l'utilisateur ${} et le créateur: Accès autorisé !", authentication.getName());
            status = true;
        } else if (this.hasRole(authentication, "admin")) {
            log.info("l'utilisateur ${} a le rôle admin: Accès autorisé !", authentication.getName());
            status = true;
        } else {
            log.info("l'utilisateur ${} n'est pas créateur et n'a aucun rôle: Accès non autorisé !", authentication.getName());
        }
        log.info("******[ Log Authorization ]**********");
        return status;

    }

    /**
     * Permet de vérifier les droits d'accès Admin de l'utilisateur
     */
    private boolean hasRole(Authentication authentication, String roleName) {

        boolean hasAdminRole = authentication.getAuthorities()
                .stream().anyMatch(authority -> authority.getAuthority().equals(roleName));

        // Vérifie si l'utilisateur possède les droits admin
        if (hasAdminRole) {
            log.info("{} authority : Accès autorisé ...", roleName);
            return true;
        }

        log.info("Cette Utilisateur : {}", authentication.getName() + " ne possède pas les droits d'accès ");
        log.info("Accès refusé ...");
        return false;
    }

    /**
     * Permet d'extraire l'ID utilisateur du token.
     *
     * @param authentication Objet provenant de Spring Security
     * @return user Id From Token
     */
    private String getUserIdFromToken(Authentication authentication) {

        String userIdFromToken = "";

        // Extraction de l'ID utilisateur du token
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            userIdFromToken = jwt.getClaimAsString("sub");
            log.info("Instance jwt , ID token: {}", userIdFromToken);

        } else if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
            KeycloakPrincipal kcPrincipal = (KeycloakPrincipal) authentication.getPrincipal();
            userIdFromToken = kcPrincipal.getName();
            log.info("Instance kcPrincipal , ID token: {}", userIdFromToken);

        } else {
            userIdFromToken = authentication.getPrincipal().toString();
            log.info("Instance is not jwt or KeycloakPrincipal, ID token: {}", userIdFromToken);
        }

        return userIdFromToken;
    }
}
