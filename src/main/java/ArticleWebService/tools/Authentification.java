package ArticleWebService.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Authentification {

    public Authentification() {

    }

    /**
     * Vérifier si l'utilisateur est le créateur ou si non si il possède les droits ADMIN.
     *
     * @param userId l'identifiant utiliseur
     * @return un boolean qui données une autorization d'accès.
     */
    public boolean isAutorization(String userId) {

        boolean identity = false;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        log.info("Username : " + authentication.getName());
        log.info("Detail : " + authentication.getDetails());
        log.info("Principal : " + authentication.getPrincipal());
        log.info("Authorities : " + authentication.getAuthorities());

        log.info("User id  : " + userId);

        // recherche si l'utiliseur et le créateur
        if (userId.equals(authentication.getPrincipal().toString())) {
            log.info("User créateur : Accès autorisé ...");
            identity = true;
        }

        // recherche si l'utilisateur a le Role ADMIN
        if (isRole(securityContext.getAuthentication())) {
            log.info("ADMIN rôle : Accès autorisé ...");
            identity = true;
        }

        return identity;
    }


    /**
     * Recherche le droit ADMIN
     *
     * @return true si le droits ADMIN et trouvé.
     */
    public static boolean isRole(Authentication authority) {


        for (GrantedAuthority autority : authority.getAuthorities()) {
            log.info("Autority : " + autority.getAuthority());
            if (autority.getAuthority().equals("ADMIN")) {
                return true;
            }
        }

        return false;

    }

}
