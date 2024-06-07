package ArticleWebService.security;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class KeycloakSecurityService extends KeycloakWebSecurityConfigurerAdapter {

    /**
     * Permet la stratégie de la gestion de session. Elle utilise implementation classic
     */
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * Permet la délégation au micro service Keycloak la gestion des utilisateurs est des rôles.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(this.keycloakAuthenticationProvider());
    }

    /**
     * Permet la gestion des droits d'accès.
     * Les request CORS (Cross-Origin) Multi-origine, est gérer par la Gateway au niveau du WebFilter
     * Les variable suivant sont donc inutile :
     * http.cors();
     * http.headers().frameOptions().disable();
     * http.headers().frameOptions().sameOrigin();
     * <p>
     * <p>
     * La protection CSRF (Cross-Site Request Forgery) standard de Spring doit être désactivé
     * pour cause de redondance avec Keycloak, le gestionnaire d'authentification.
     * La déactivation : http.csrf().disable();
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // configure plusieurs éléments essentiels pour l'intégration de Keycloak avec Spring Security
        super.configure(http);

        // Utilise keycloak pour la sécurité donc désactive
        http.csrf().disable();

        // Gestion des accès au ressources
        http.authorizeRequests()
                .antMatchers("/article/getAllArticles",
                        "/article/saveImages",
                        "/article/upload",
                        "/article/getAllArticlesSection",
                        "/article/getAllDomain")
                .permitAll();

        // role authorisé sur les endpoint user
        http.authorizeRequests()
                .antMatchers("/article/saveArticle")
                .hasAuthority("user");

        // role authorisé sur les endpoint user et admin
        http.authorizeRequests()
                .antMatchers("/article/updateArticle", "/article/deleteArticle/* ")
                .hasAnyAuthority("admin", "user");
    }


}
