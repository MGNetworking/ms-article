package ArticleWebService.web.unitaire;

import static org.junit.jupiter.api.Assertions.*;

import ArticleWebService.dto.ArticleDtoSave;
import ArticleWebService.entities.Section;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Classe de test unitaire classique pure.
 * Ils n'ont pas besoin de context Spring et donc ne dépend pas
 * de conteneur Spring.
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
    @DisplayName("Validation échoue pour un article invalide")
    void testSaveArticleValidationFails() {
        // Créez un objet ArticleSave invalide
        ArticleDtoSave articleValidation = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                "Titre valide",
                "image.png",
                "Ceci est une description",
                "description de l'article",
                "",
                true
        );

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(articleValidation);

        // Vérification des erreurs
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        // Optionnel : Vérifiez les messages d'erreur spécifiques
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("L'article doit avoir un contenu")));

    }


    @Test
    @DisplayName("Validation échoue pour un titre d'article invalide")
    void testSaveTitleArticleValidationFails() {
        // Créez un objet ArticleSave invalide
        ArticleDtoSave articleValidation = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                "",
                "image.png",
                "Ceci est une description",
                "description de l'article",
                "Ceci est le contenu de l'article",
                true
        );

        // Simulez la validation manuelle
        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(articleValidation);
        System.out.println("violations is : " + violations);

        // Vérification des erreurs
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());

        // Optionnel : Vérifiez les messages d'erreur spécifiques
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Vous devez mettre un titre")));
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Le titre doit contenir entre 3 et 100 caractères.")));
    }

    @Test
    @DisplayName("Validation échoue pour un idUser invalide")
    void testInvalidIdUserValidation() {
        ArticleDtoSave article = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                null, // idUser null
                "Titre valide",
                "image.png",
                "Ceci est une description",
                "Description de l'article",
                "Contenu de l'article",
                true
        );

        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(article);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage()
                .equals("L'identifiant utilisateur est absent")));
    }

    @Test
    @DisplayName("Validation échoue pour une section invalide")
    void testInvalidSectionValidation() {
        ArticleDtoSave article = new ArticleDtoSave(
                null,
                null, // Section null
                "user123",
                "Titre valide",
                "image.png",
                "Ceci est une description",
                "Description de l'article",
                "Contenu de l'article",
                true
        );

        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(article);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage()
                        .equals("La section de l'article est obligatoire ")));
    }

    @Test
    @DisplayName("Validation échoue pour imgUrl invalide")
    void testInvalidImgUrlValidation() {
        ArticleDtoSave article = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                "Titre valide",
                "", // imgUrl vide
                "Ceci est une description",
                "Description de l'article",
                "Contenu de l'article",
                true
        );

        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(article);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage()
                        .equals("l'URL de l'image n'est pas présent")));
    }

    @Test
    @DisplayName("Validation échoue pour imgDescription invalide")
    void testInvalidImgDescriptionValidation() {
        ArticleDtoSave article = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                "Titre valide",
                "image.png",
                "", // imgDescription vide
                "Description de l'article",
                "Contenu de l'article",
                true
        );

        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(article);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("La description doit être fourni")));
    }

    @Test
    @DisplayName("Validation échoue pour une description invalide")
    void testInvalidDescriptionValidation() {
        ArticleDtoSave article = new ArticleDtoSave(
                null,
                new Section(1, "Java"),
                "user123",
                "Titre valide",
                "image.png",
                "Ceci est une description",
                "", // description vide
                "Contenu de l'article",
                true
        );

        Set<ConstraintViolation<ArticleDtoSave>> violations = validator.validate(article);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("L'article ne doit avoir un contenu")));
    }

}
