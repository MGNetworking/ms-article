package ArticleWebService.unitaire;

import static org.junit.jupiter.api.Assertions.*;

import ArticleWebService.dto.ArticleDtoSave;

import java.lang.String;

import ArticleWebService.entities.Section;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe de test unitaire classique pure.
 * Ils n'ont pas besoin de context Spring et donc ne dépend pas de conteneur Spring.
 */

@ActiveProfiles("test")
@Slf4j
public class ArticleValidatorDtoSaveTest {

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
    @Order(1)
    @DisplayName("Validation de l'enregistrement d'un article")
    void testSaveArticleValidationOk() {

        // Créez un objet ArticleSave invalide
        ArticleDtoSave articleValidation = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                "Titre valide",
                "https://image.png",
                "Ceci est une description",
                "description de l'article",
                "Un article qui contient du text",
                true,
                false
        );

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(articleValidation);

        // récupérer les données en erreur
        List<String> errors = violations.stream()
                .map(v -> "Propriété : " + v.getPropertyPath() + ", Message : " + v.getMessage())
                .collect(Collectors.toList());

        // Si des erreurs sont trouvé, elles seront affiché dans les résultats
        assertTrue(violations.isEmpty(), errors.toString());
    }

    @Test
    @Order(2)
    @DisplayName("Validation en échec pour cause de données non valide")
    void testValidationEchecAllChamps() {

        // Créez un objet ArticleSave invalide
        ArticleDtoSave articleValidation = new ArticleDtoSave(
                null,
                null,
                "",
                "",
                "",
                "",
                "",
                "",
                true,
                false);

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(articleValidation);

        // Récupérez les erreurs sous forme lisible
        Map<String, List<String>> errorsByField = violations.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        System.out.println("Liste des erreurs retourné : " + errorsByField);

        // Vérifiez les erreurs pour chaque champ
        assertTrue(errorsByField.containsKey("section"),
                "Le champ 'section' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("section").contains("La section de l'article est obligatoire "),
                "Le message attendu pour 'section' est manquant. Erreurs trouvées : " + errorsByField.get("section"));

        assertTrue(errorsByField.containsKey("idUser"),
                "Le champ 'idUser' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("idUser").contains("L'identifiant utilisateur est absent"),
                "Le message attendu pour 'idUser' est manquant. Erreurs trouvées : " + errorsByField.get("idUser"));

        assertTrue(errorsByField.containsKey("titre"),
                "Le champ 'titre' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("titre").contains("Vous devez mettre un titre"),
                "Le message attendu pour 'titre' manquant. Erreurs trouvées : " + errorsByField.get("titre"));

        assertTrue(errorsByField.containsKey("imgUrl"),
                "Le champ 'imgUrl' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("imgUrl").contains("l'URL de l'image n'est pas présent")
                        && errorsByField.get("imgUrl").contains("L'URL de l'image doit être une URL valide"),
                "Le message attendu pour 'imgUrl' est manquant. Erreurs trouvées : " + errorsByField.get("imgUrl"));

        assertTrue(errorsByField.containsKey("imgDescription"),
                "Le champ 'imgDescription' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("imgDescription").contains("La description doit être fourni"),
                "Le message attendu pour 'imgDescription' est manquant. Erreurs trouvées : " + errorsByField.get("imgDescription"));

        assertTrue(errorsByField.containsKey("description"),
                "Le champ 'description' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("description").contains("L'article ne doit avoir un contenu"),
                "Le message attendu pour 'description' est manquant. Erreurs trouvées : " + errorsByField.get("description"));

        assertTrue(errorsByField.containsKey("article"),
                "Le champ 'article' devrait être en erreur. Erreurs trouvées : " + errorsByField);
        assertTrue(errorsByField.get("article").contains("L'article doit avoir un contenu"),
                "Le message attendu pour 'article' est manquant. Erreurs trouvées : " + errorsByField.get("article"));

        // Vérifiez le nombre total d'erreurs
        assertEquals(9, violations.size(),
                "Nombre d'erreurs attendu incorrect. Erreurs trouvées : " + errorsByField);

    }

    @Test
    @Order(3)
    @DisplayName("Validation titre trop petit")
    void TestValidationTitreEmpty() {

        // Créez un objet ArticleSave invalide
        ArticleDtoSave articleValidation = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                "ab",
                "https://image.png",
                "Ceci est une description",
                "description de l'article",
                "",
                true,
                false
        );

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(articleValidation);

        // Récupérez les erreurs sous forme lisible
        Map<String, List<String>> errorsByField = violations.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        assertTrue(errorsByField.get("titre").contains("Le titre doit contenir entre 3 et 100 caractères."),
                "Le message attendu pour 'titre' (vide) est manquant. Erreurs trouvées : " + errorsByField.get("titre"));
    }

    @Test
    @Order(4)
    @DisplayName("Validation titre trop grand")
    void TestValidationTitreTooBig() {

        StringBuilder titre = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            titre.append("a");
        }
        // Créez un objet ArticleSave invalide
        ArticleDtoSave articleValidation = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                titre.toString(),
                "image.png",
                "Ceci est une description",
                "description de l'article",
                "",
                true,
                false
        );

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(articleValidation);

        // Récupérez les erreurs sous forme lisible
        Map<String, List<String>> errorsByField = violations.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        assertTrue(errorsByField.get("titre").contains("Le titre doit contenir entre 3 et 100 caractères."),
                "Le message attendu pour 'titre' trop grand. Erreurs trouvées : " + errorsByField.get("titre"));
    }
}
