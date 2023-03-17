package ArticleWebService.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Authentification {

    public Authentification(){

    }

    public boolean userCreatorArticle(String userId) {

        boolean identity = false;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        log.info("Username : " + authentication.getName());
        log.info("Detail : " + authentication.getDetails());
        log.info("Principal : " + authentication.getPrincipal());
        log.info("Authorities : " + authentication.getAuthorities());

        log.info("User id  : " + userId);

        if (userId.equals(authentication.getPrincipal().toString())){
            log.info("Autorisation ok ..." );
            identity = true;
        }

        return identity;
    }

}
