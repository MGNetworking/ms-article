package ArticleWebService.security;

import ArticleWebService.entities.Article;
import ArticleWebService.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Component
@Slf4j
public class Authentification {

    @Autowired
    private ArticleService articleService;

    public Authentification() {

    }

    /**
     * Permet de comparer l'id utilisateur passé en paramètre avec l'id
     * utilisateur contenu dans le token présent dans la requête.
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
        if (userId.equals(this.getUserIdFromToken(authentication))) {
            log.info("User créateur : Accès autorisé !");
            return true;
        } else if (this.hasRole(authentication, "admin")) {
            log.info("User admin : Accès autorisé !");
            return true;
        } else {
            log.info("User créateur : Accès non autorisé !");
            return false;
        }

    }

    /**
     * Recherche le créateur de l'article par son id et vérifie son authenticité
     * ou vérifie si l'utilisateur possède le rôle admin.
     * <p>
     * Si l'auteur et bien le créateur de l'article ou possède le rôle admin,
     * il sera authorisé à supprimer cet article.
     *
     * @param idArticle L'ID de l'article.
     * @return true si l'utilisateur est authorisé à supprimer l'article
     */
    public boolean deleteArticle(Integer idArticle) {

        // Recupération dans Thread local le context de sécurité de Spring en cours
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        // requête vers la bd
        Optional<Article> article = this.articleService.findArticleById(idArticle);

        // Si l'article est bien présent
        if (article.isPresent()) {

            String userId = article.get().getIdUser();
            log.info("l'article est présent avec l'ID user: {}", userId);

            // Compare l'id présent dans le token et l'id utilisateur présent dans l'article
            if (authentication.getPrincipal().toString().equals(userId)) {
                log.info("L'utilisateur est bien bien le créateur de l'article, l'article peut être supprimer ");
                return true;
            }

            // Teste des droites sur le role admin
            return this.hasRole(authentication, "admin");

        }

        return false;
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
