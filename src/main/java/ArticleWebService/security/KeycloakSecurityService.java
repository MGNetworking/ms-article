package ArticleWebService.security;

import ArticleWebService.filter.PostApiFilter;
import ArticleWebService.filter.PreApiFilter;
import ArticleWebService.handler.security.CustomAccessDeniedHandler;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@KeycloakConfiguration
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class KeycloakSecurityService extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

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
     * Configure la sécurité HTTP pour gérer les droits d'accès et intégrer Keycloak avec Spring Security.
     *
     * <h2>Gestion des requêtes CORS</h2>
     * Les requêtes CORS (Cross-Origin Resource Sharing) multi-origine sont gérées par la Gateway au niveau du WebFilter.
     * Par conséquent, les configurations suivantes sont inutiles et ont été désactivées :
     * <ul>
     *     <li><code>http.cors()</code></li>
     *     <li><code>http.headers().frameOptions().disable()</code></li>
     *     <li><code>http.headers().frameOptions().sameOrigin()</code></li>
     * </ul>
     *
     * <h2>Protection CSRF</h2>
     * La protection CSRF standard de Spring Security est désactivée car elle est redondante avec Keycloak,
     * qui agit comme gestionnaire d'authentification. La désactivation est réalisée via :
     * <pre><code>http.csrf().disable()</code></pre>
     *
     * <h2>Gestion des autorisations</h2>
     * Les autorisations sont configurées comme suit :
     * <ul>
     *     <li>Les endpoints publics accessibles sans authentification :
     *         <ul>
     *             <li><code>/article/getAllArticles</code></li>
     *             <li><code>/article/saveImages</code></li>
     *             <li><code>/article/upload</code></li>
     *             <li><code>/article/getAllArticlesSection</code></li>
     *             <li><code>/article/getAllDomain</code></li>
     *         </ul>
     *     </li>
     *     <li>L'accès à <code>/article/saveArticle</code> est restreint aux utilisateurs ayant l'autorité <code>user</code>.</li>
     *     <li>L'accès à <code>/article/updateArticle</code> et <code>/article/deleteArticle/*</code> est réservé aux utilisateurs
     *         ayant l'une des autorités suivantes : <code>admin</code> ou <code>user</code>.</li>
     * </ul>
     *
     * <h2>Filtres personnalisés</h2>
     * Deux filtres personnalisés sont ajoutés pour enrichir le traitement des requêtes :
     * <ul>
     *     <li><code>PreApiFilter</code> : exécuté avant le filtre <code>BasicAuthenticationFilter</code>.</li>
     *     <li><code>PostApiFilter</code> : exécuté après le filtre <code>BasicAuthenticationFilter</code>.</li>
     * </ul>
     *
     * @param http l'objet {@link HttpSecurity} permettant de configurer les règles de sécurité HTTP
     * @throws Exception si une erreur survient lors de la configuration de la sécurité HTTP
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // configure plusieurs éléments essentiels pour l'intégration de Keycloak avec Spring Security
        super.configure(http);

        http
                .cors().disable()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/articles/getAllArticles",
                        "/articles/saveImages",
                        "/articles/upload",
                        "/articles/getAllArticlesSection",
                        "/articles/getAllDomain")
                .permitAll()
                .antMatchers("/articles/saveArticle")
                .hasAuthority("user")
                .antMatchers(
                        "/articles/updateArticle",
                        "/articles/deleteArticle/*",
                        "/articles/update/*")
                .hasAnyAuthority("admin", "user")
                .and()
                .addFilterBefore(new PreApiFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new PostApiFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling().accessDeniedHandler(this.accessDeniedHandler);


    }


}
