package ArticleWebService.web.unitaire;


import ArticleWebService.security.Authentification;
import ArticleWebService.service.ArticleService;
import ArticleWebService.web.SystemPropertiesActiveProfileResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Cette Classe test les authorisations
 */
@SpringBootTest
@ActiveProfiles(resolver = SystemPropertiesActiveProfileResolver.class)
class AuthentificationTest {

    private Authentification authentification;

    @MockBean
    private ArticleService articleService; // MockBean for dependency injection

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authentification = new Authentification();

        SecurityContextHolder.clearContext();
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    @DisplayName("L'utilisateur connecter est autoriser a modifier l'article")
    void user_Is_Authorization() {

        // Simule les données envoié par le demander possèdent un token
        // L'utilisateur connecté qui cherche à apporter des modifications sur un article
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "max", // principal
                null, // credentials (le token brut)
                 "user", "commentaire" // authorities
        );

        // Associer l'objet d'authentification au contexte de sécurité
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // L'auteur de l'article à modifier
        boolean result = authentification.isAuthorization("max");
        assertTrue(result,
                "L'utilisateur est autoriser a modifier l'article puisqu'il en est le propriétaire");
    }

    @Test
    @DisplayName("l'utilisateur connecter est autoriser a modifier l'article avec le rôle admin")
    void user_Is_Authorization_with_adminRole() {

        // Simule les données envoié par le demander possèdent un token
        // L'utilisateur connecté qui cherche à apporter des modifications sur un article
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "max", // principal
                null, // credentials (le token brut)
                "admin", "user", "commentaire" // authorities
        );

        // Associer l'objet d'authentification au contexte de sécurité
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // L'auteur de l'article à modifier
        boolean result = authentification.isAuthorization("maximus");
        assertTrue(result,
                "L'utilisateur est autoriser a modifier l'article puisqu'il a le role admin ");
    }

    @Test
    @DisplayName("L'utilisateur connecter n'est pas autoriser a modifier l'article")
    void user_IsNot_Authorization() {

        // Simule les données envoié par le demander possèdent un token
        // L'utilisateur connecté qui cherche à apporter des modifications sur un article
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "max", // principal
                null, // credentials (le token brut)
                 "user", "commentaire" // authorities
        );

        // Associer l'objet d'authentification au contexte de sécurité
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // L'auteur de l'article à modifier
        boolean result = authentification.isAuthorization("maximus");
        assertFalse(result,
                "L'utilisateur n'est pas autoriser puisqu'il n'a le role admin n'est pas le propriétaire ");
    }

}
