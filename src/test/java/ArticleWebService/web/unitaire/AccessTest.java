package ArticleWebService.web.unitaire;

import ArticleWebService.security.Access;
import ArticleWebService.service.ArticleService;
import ArticleWebService.web.SystemPropertiesActiveProfileResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Classe de test pour vérifier les règles d'autorisation d'accès aux articles.
 *
 * <p>
 * Cette classe teste différents cas d'utilisation liés à l'autorisation d'un utilisateur
 * à modifier un article, en fonction de son rôle ou de son identité.
 * </p>
 *
 * <p>
 * Fonctionnalités testées :
 * <ul>
 *   <li>Un utilisateur est autorisé à modifier un article dont il est le propriétaire.</li>
 *   <li>Un utilisateur est autorisé à modifier un article s'il possède le rôle administrateur.</li>
 *   <li>Un utilisateur n'est pas autorisé à modifier un article s'il n'a ni le rôle administrateur ni la propriété de l'article.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Cette classe utilise les annotations suivantes pour faciliter les tests :
 * <ul>
 *   <li>{@link ExtendWith} : Fournit une extension Mockito pour la gestion des mocks.</li>
 *   <li>{@link ActiveProfiles} : Active un profil spécifique pour les tests, basé sur les propriétés système.</li>
 *   <li>{@link MockBean} : Injecte des dépendances simulées.</li>
 *   <li>{@link InjectMocks} : Injecte les dépendances nécessaires dans la classe testée.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Configuration avant chaque test :
 * <ul>
 *   <li>Le contexte de sécurité est nettoyé pour garantir l'isolement des tests.</li>
 * </ul>
 * </p>
 *
 * @see ExtendWith
 * @see ActiveProfiles
 * @see MockBean
 * @see InjectMocks
 * @see SecurityContextHolder
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AccessTest {

    private Access access;

    @Mock
    private ArticleService articleService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Nettoyer le contexte de sécurité avant chaque test
        SecurityContextHolder.clearContext();
        this.access = new Access();
    }

    /**
     * Teste si un utilisateur connecté est autorisé à modifier un article.
     *
     * <p>
     * Ce test simule un contexte de sécurité Spring pour un utilisateur connecté avec un token JWT
     * et vérifie que l'accès à la modification d'un article est autorisé pour son propriétaire.
     * </p>
     *
     * <p>
     * Cas testé :
     * <ul>
     *   <li>L'utilisateur connecté est identifié comme étant le propriétaire de l'article à modifier.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Scénario :
     * <ol>
     *   <li>Un utilisateur avec des autorités spécifiques est simulé via {@link TestingAuthenticationToken}.</li>
     *   <li>Le contexte de sécurité est configuré avec cet utilisateur.</li>
     *   <li>La méthode {@code access.isAuthorization()} est appelée pour vérifier si l'utilisateur est autorisé à modifier l'article.</li>
     *   <li>Une assertion est effectuée pour valider le comportement attendu.</li>
     * </ol>
     * </p>
     *
     * <p>
     * Conditions :
     * <ul>
     *   <li>L'utilisateur possède un rôle ou une autorité suffisante pour modifier l'article.</li>
     * </ul>
     * </p>
     *
     * @see TestingAuthenticationToken
     * @see SecurityContextHolder
     * @see SecurityContext
     */
    @Test
    @DisplayName("L'utilisateur connecter est autoriser à modifier l'article")
    void user_Is_Authorization() {

        // Simule les données envoié par le demander possèdent un token
        // L'utilisateur connecté qui cherche à apporter des modifications sur un article
        Authentication authentication = new TestingAuthenticationToken(
                "max", // Principal : le JWT simulé
                null, // credentials (le token brut)
                "user", "commentaire" // authorities
        );

        // initialisation du context de sécurité Spring
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // L'auteur de l'article à modifier
        boolean result = this.access.isAuthorization("max");
        assertTrue(result,
                "L'utilisateur est autoriser a modifier l'article puisqu'il en est le propriétaire");
    }

    /**
     * Teste si un utilisateur connecté avec le rôle administrateur est autorisé à modifier un article.
     *
     * <p>
     * Ce test simule un contexte de sécurité Spring pour un utilisateur avec un rôle d'administrateur
     * et vérifie que cet utilisateur est autorisé à modifier un article, même s'il n'en est pas le propriétaire.
     * </p>
     *
     * <p>
     * Cas testé :
     * <ul>
     *   <li>L'utilisateur connecté possède un rôle administrateur qui lui confère des privilèges élevés.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Scénario :
     * <ol>
     *   <li>Un utilisateur avec un rôle d'administrateur est simulé via {@link TestingAuthenticationToken}.</li>
     *   <li>Le contexte de sécurité est configuré avec cet utilisateur.</li>
     *   <li>La méthode {@code access.isAuthorization()} est appelée pour vérifier si l'utilisateur est autorisé à modifier l'article.</li>
     *   <li>Une assertion est effectuée pour valider que l'accès est accordé en raison du rôle d'administrateur.</li>
     * </ol>
     * </p>
     *
     * <p>
     * Conditions :
     * <ul>
     *   <li>L'utilisateur possède le rôle ou l'autorité "admin".</li>
     * </ul>
     * </p>
     *
     * @see TestingAuthenticationToken
     * @see SecurityContextHolder
     * @see SecurityContext
     */
    @Test
    @DisplayName("L'utilisateur connecter est autoriser à modifier l'article avec le rôle admin")
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
        boolean result = access.isAuthorization("maximus");
        assertTrue(result,
                "L'utilisateur est autoriser a modifier l'article puisqu'il a le role admin ");
    }

    /**
     * Teste si un utilisateur connecté sans les droits nécessaires n'est pas autorisé à modifier un article.
     *
     * <p>
     * Ce test simule un contexte de sécurité Spring pour un utilisateur qui n'est ni propriétaire de l'article
     * ni détenteur d'un rôle administratif, et vérifie qu'il ne peut pas modifier l'article.
     * </p>
     *
     * <p>
     * Cas testé :
     * <ul>
     *   <li>L'utilisateur connecté n'est pas autorisé car il n'a pas les droits requis.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Scénario :
     * <ol>
     *   <li>Un utilisateur standard est simulé via {@link TestingAuthenticationToken} avec des droits limités.</li>
     *   <li>Le contexte de sécurité est configuré avec cet utilisateur.</li>
     *   <li>La méthode {@code access.isAuthorization()} est appelée pour vérifier si l'utilisateur est autorisé à modifier l'article.</li>
     *   <li>Une assertion est effectuée pour valider que l'accès est refusé.</li>
     * </ol>
     * </p>
     *
     * <p>
     * Conditions :
     * <ul>
     *   <li>L'utilisateur ne possède ni le rôle "admin" ni la propriété de l'article.</li>
     * </ul>
     * </p>
     *
     * @see TestingAuthenticationToken
     * @see SecurityContextHolder
     * @see SecurityContext
     */
    @Test
    @DisplayName("L'utilisateur connecter n'est pas autoriser a modifier l'article")
    void user_IsNot_Authorization() {

        // Création d'un context de sécurité
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        // Associer l'objet d'authentification au contexte de sécurité
        securityContext.setAuthentication(new TestingAuthenticationToken(
                "max", // principal
                null, // credentials (le token brut)
                "user", "commentaire" // authorities
        ));
        SecurityContextHolder.setContext(securityContext);

        // L'auteur de l'article à modifier
        boolean result = access.isAuthorization("maximus");
        assertFalse(result,
                "L'utilisateur n'est pas autoriser puisqu'il n'a le role admin et n'est pas le propriétaire ");
    }
}
