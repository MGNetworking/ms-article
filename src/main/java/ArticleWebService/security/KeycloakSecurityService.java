package ArticleWebService.security;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
public class KeycloakSecurityService extends KeycloakWebSecurityConfigurerAdapter {

    /**
     * Permet la stratégie de la gestion de session
     * Elle utilise implementation classic
     *
     * @return
     */
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * Permet de déléguer au micro service Keycloak la gestion des utilisateurs est des rôles.
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(this.keycloakAuthenticationProvider());
    }

    /**
     * permet la gestion des droits d'accées
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        super.configure(http); // garde la conf par defaut

        // acces sans droit
        http.authorizeRequests()
                .antMatchers("/getListArticle")
                .permitAll();

        // acces avec droits
        http.authorizeRequests()
                .antMatchers("/saveArticle")
                .hasRole("article");
                //.authenticated();
    }
}
