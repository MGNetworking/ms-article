package ArticleWebService.unitaire;

import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.Section;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test unitaire classique pure.
 * Ils n'ont pas besoin de context Spring et donc ne dépend pas de conteneur Spring.
 */

@ActiveProfiles("test")
@Slf4j
public class ArticleValidatorDtoUpdateTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Initialisation du validateur
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        } catch (Exception ex) {
            System.out.println("Une erreur est survenu pendant initialisation du validateur");
        }
    }

    @Test
    @DisplayName("Validation Ok de la mise a jours des donnéées de l'article")
    void testUpdateValidationOk() {


        // Créez un objet ArticleSave invalide
        ArticleDtoUpdate articleValidation = new ArticleDtoUpdate(
                1,
                "user1",
                new Section(1, "Java"),
                "user123",
                "https://Titre valide",
                "image.png",
                "Ceci est une description",
                "description de l'article",
                false,
                false,
                1,
                new Timestamp(System.currentTimeMillis()),
                null
        );

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoUpdate>> violations = validator.validate(articleValidation);

        // récupérer les données en erreur
        List<String> errors = violations.stream()
                .map(v -> "Propriété : " + v.getPropertyPath() + ", Message : " + v.getMessage())
                .collect(Collectors.toList());

        // Si des erreurs sont trouvé, elles seront affiché dans les résultats
        assertTrue(violations.isEmpty(), errors.toString());

    }

    @Test
    @DisplayName("Validation en échec pour cause de donnée non valide")
    void testValidationEchecPlusieursChamps() {


        // Créez un objet ArticleSave invalide
        ArticleDtoUpdate articleValidation = new ArticleDtoUpdate(
                null,
                null,
                null,
                "",
                "",
                "",
                "",
                "",
                false,
                false,
                2,
                null,
                null
        );

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoUpdate>> violations = validator.validate(articleValidation);

        // Récupérez les erreurs sous forme lisible
        Map<String, List<String>> errorsByField = violations.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        // Vérifiez les erreurs pour chaque champ avec un message d'échec détaillé
        assertTrue(errorsByField.containsKey("idArticle"),
                "Le champ 'idArticle' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("idArticle").contains("L'identifiant de l'article est absent "),
                "Le message attendu pour 'idArticle' est manquant. Erreurs trouvées : " + errorsByField.get("idArticle"));

        assertTrue(errorsByField.containsKey("idUser"),
                "Le champ 'idUser' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("idUser").contains("L'identifiant utilisateur est absent"),
                "Le message attendu pour 'idUser' est manquant. Erreurs trouvées : " + errorsByField.get("idUser"));

        assertTrue(errorsByField.containsKey("section"),
                "Le champ 'section' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("section").contains("La section de l'article est obligatoire "),
                "Le message attendu pour 'section' est manquant. Erreurs trouvées : " + errorsByField.get("section"));

        assertTrue(errorsByField.containsKey("titre"),
                "Le champ 'titre' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("titre").contains("l'article doit avoir un titre "),
                "Le message attendu pour 'titre' est manquant. Erreurs trouvées : " + errorsByField.get("titre"));

        assertTrue(errorsByField.containsKey("imgUrl"),
                "Le champ 'imgUrl' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("imgUrl").contains("l'URL de l'image n'est pas présent"),
                "Le message attendu pour 'imgUrl' est manquant. Erreurs trouvées : " + errorsByField.get("imgUrl"));

        assertTrue(errorsByField.containsKey("imgDescription"),
                "Le champ 'imgDescription' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("imgDescription").contains("La description doit être fourni"),
                "Le message attendu pour 'imgDescription' est manquant. Erreurs trouvées : " + errorsByField.get("imgDescription"));

        assertTrue(errorsByField.containsKey("description"),
                "Le champ 'description' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("description").contains("L'article doit avoir une description"),
                "Le message attendu pour 'description' est manquant. Erreurs trouvées : " + errorsByField.get("description"));

        assertTrue(errorsByField.containsKey("article"),
                "Le champ 'article' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("article").contains("L'article doit avoir un contenu"),
                "Le message attendu pour 'article' est manquant. Erreurs trouvées : " + errorsByField.get("article"));

        assertTrue(errorsByField.containsKey("vue"),
                "Le champ 'vue' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("vue").contains("La valeur de 'vue' ne doit pas dépasser 1"),
                "Le message attendu pour 'vue' est manquant. Erreurs trouvées : " + errorsByField.get("vue"));

        assertTrue(errorsByField.containsKey("dateCreation"),
                "Le champ 'dateCreation' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("dateCreation").contains("La date de création est obligatoire"),
                "Le message attendu pour 'dateCreation' est manquant. Erreurs trouvées : " + errorsByField.get("dateCreation"));

    }
}
