package ArticleWebService.security;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
@Slf4j
public class KeycloakSecurityService extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    private CustomAuthorizationFilter customAuthFilter;

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
     * Permet la délégation au micro service Keycloak la gestion des utilisateurs est des rôles.
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(this.keycloakAuthenticationProvider());
    }

    /**
     * Permet la gestion des droits d'accées.
     * Les request Cros (Cross Origin) Est gérer par la Gateway au niveau du WebFilter
     * Les variable suivant sont inutile :
     * http.cors();
     * http.headers().frameOptions().disable();
     * http.headers().frameOptions().sameOrigin();
     * <p>
     * Doit être désactivé :
     * http.csrf().disable();
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // garde la conf par defaut
        super.configure(http);

        http.csrf().disable();

        // gestion des accès au ressources
        http.authorizeRequests()
                .antMatchers("/article/getAllArticles",
                        "/article/saveImages",
                        "/article/getAllArticlesSection",
                        "/article/getAllDomain")
                .permitAll();
        // USER role
        http.authorizeRequests()
                .antMatchers("/article/saveArticle")
                .hasAuthority("USER");

        // USER and ADMIN Roles
        http.authorizeRequests()
                .antMatchers("/article/updateArticle",
                        "/article/deleteArticle/* ")
                .hasAnyAuthority("ADMIN", "USER");


        // filtre personnalilsé
        http.addFilterAfter(this.customAuthFilter,
                KeycloakSecurityContextRequestFilter.class);
    }


}
