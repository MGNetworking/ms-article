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

        // Test l'id user paramètre avec l'id User token
        if (userId.equals(this.getUserIdFromToken(authentication))) {
            log.info("User créateur : Accès autorisé ...");
            return true;
        }

        return this.hasRole(authentication, "admin");

    }

    /**
     * Recherche le créateur de l'article par son id verifi si il bien l'auteur ou si il est admin.
     * Si il est créateur ou admin, il est authorisé a supprimer cette article.
     *
     * @param idArticle
     * @return true si l'utilisateur est authorisé a supprimer l'article
     */
    public boolean deleteArticle(Integer idArticle) {

        // recupération dans theadlocal le context de sécurité de Spring en cours
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        // requête vers la bd
        Optional<Article> article = this.articleService.findArticleById(idArticle);

        // Si l'article est bien présent
        if (article.isPresent()) {

            String userId = article.get().getIdUser();
            log.info("l'article est présent avec l'ID user : " + userId);

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
     *
     * @param authentication
     * @return
     */
    private boolean hasRole(Authentication authentication, String roleName) {

        boolean hasAdminRole = authentication.getAuthorities()
                .stream().anyMatch(authority -> authority.getAuthority().equals(roleName));

        // Verifie si l'utilisateur est a les droits admin
        if (hasAdminRole) {
            log.info(roleName + " authority : Accès autorisé ...");
            return true;
        }

        log.info("Cette Utilisateur : " + authentication.getName() + " ne possède pas les droits d'accès ");
        log.info("Accès refusé ...");
        return false;
    }

    /**
     * Permet d'extraire l'ID utilisateur du token.
     *
     * @param authentication
     * @return user Id From Token
     */
    private String getUserIdFromToken(Authentication authentication) {

        String userIdFromToken = "";

        // Extraction de l'ID utilisateur du token
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            userIdFromToken = jwt.getClaimAsString("sub");
            log.info("Instance jwt , ID token  : " + userIdFromToken);

        } else if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
            KeycloakPrincipal kcPrincipal = (KeycloakPrincipal) authentication.getPrincipal();
            userIdFromToken = kcPrincipal.getName();
            log.info("Instance kcPrincipal , ID token  : " + userIdFromToken);

        } else {
            userIdFromToken = authentication.getPrincipal().toString();
            log.info("Instance is not jwt or KeycloakPrincipal , ID token : " + userIdFromToken);
        }

        return userIdFromToken;
    }
}
