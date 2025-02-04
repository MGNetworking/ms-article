package ArticleWebService.web.unitaire;

import ArticleWebService.dto.ErrorDetail;
import ArticleWebService.handler.Exception.ArticleException;
import ArticleWebService.handler.Exception.GlobalExceptionHandler;
import ArticleWebService.handler.Exception.InvalidPathVariableException;
import ArticleWebService.handler.response.GenericApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;


    @BeforeEach
    void setUp() {
        this.globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handle Article Exception - NOT_FOUND")
    public void testHandleArticleExceptionTest() {
        // Arrange
        ArticleException ex = new ArticleException("l'article n'a pas été trouvé", HttpStatus.NOT_FOUND);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/not-found");
        WebRequest webRequest = new ServletWebRequest(request);

        // Act
        ResponseEntity<GenericApiResponse<ErrorDetail>> responseEntity = this.globalExceptionHandler
                .handleArticleException(ex, webRequest);

        // assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        Assertions.assertEquals(
                "Veuillez contacter l'administrateur système!",
                Objects.requireNonNull(responseEntity
                                .getBody())
                        .getData()
                        .getMessage());

    }

    @Test
    @DisplayName("handle Generic Exception Test - INTERNAL_SERVER_ERROR ")
    public void testHandleGenericExceptionTest() {

        // Arrange
        Exception ex = new Exception("Message: une erreur technique est survenu",
                new Throwable("Cause: Erreur technique "));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/generic-exception");

        // Act
        ResponseEntity<GenericApiResponse<ErrorDetail>> responseEntity = this.globalExceptionHandler
                .handleGenericException(ex, request);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        Assertions.assertEquals(
                "Veuillez contacter l'administrateur système!",
                Objects.requireNonNull(responseEntity
                        .getBody()).getData().getMessage());

    }

    @Test
    @DisplayName("handle Generic Exception Test - With manage to throw AccessDenied Exception ")
    public void testHandleGenericExceptionAccessDeniedExceptionTest() {

        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Message: une erreur technique est survenu",
                new Throwable("Cause: Erreur technique "));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/generic-exception");

        // Act & Assert
        Assertions.assertThrows(AccessDeniedException.class, () -> globalExceptionHandler
                .handleGenericException(ex, request));

    }

    @Test
    @DisplayName("handle Invalid Path Variable Exception ")
    public void testHandleInvalidPathVariableException() {

        // Arrange
        InvalidPathVariableException ex = new InvalidPathVariableException("Invalid path ", "raison ");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/invalid-path");

        // Act
        ResponseEntity<GenericApiResponse<ErrorDetail>> responseEntity = this.globalExceptionHandler
                .handleInvalidPathVariableException(ex, request);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        Assertions.assertEquals(
                "URL invalide ou non conforme",
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
        Assertions.assertEquals(
                "Veuillez contacter l'administrateur système!",
                Objects.requireNonNull(responseEntity
                        .getBody()).getData().getMessage());

    }

    @Test
    @DisplayName("handle Method Argument Type Mismatch Exception ")
    public void testHandleMethodArgumentTypeMismatchExceptionTest() {

        // Arrange
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "value", String.class, "param", null, new IllegalArgumentException("Invalid type"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/invalid-argument");

        // Act
        ResponseEntity<GenericApiResponse<ErrorDetail>> responseEntity = this.globalExceptionHandler
                .handleMethodArgumentTypeMismatchException(ex, request);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assertions.assertTrue(Objects.requireNonNull(responseEntity.getBody())
                .getData().getInfo().contains("Invalid type"));
    }

    @Test
    @DisplayName("handle Constraint Violation Exception ")
    public void testHandleConstraintViolationException() {

        // Arrange
        // Création des premières erreurs de validation
        ConstraintViolation<String> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("name");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("Le nom est trop court");

        // Création d'une deuxième erreur de validation
        ConstraintViolation<String> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("email");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("L'email est invalide");

        // On met les violations dans un set pour simuler plusieurs erreurs
        Set<ConstraintViolation<String>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);

        // Création de l'exception avec plusieurs erreurs
        ConstraintViolationException ex = new ConstraintViolationException(
                "Validation error", violations);

        // Simulation de la requête HTTP
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/validate");

        // Act
        // Exécution de la méthode qui gère les erreurs de validation
        ResponseEntity<GenericApiResponse<Map<String, String>>> response =
                globalExceptionHandler.handleConstraintViolationException(ex, mockRequest);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Le nom est trop court",
                Objects.requireNonNull(response.getBody()).getData().get("name"));
        Assertions.assertEquals("L'email est invalide",
                Objects.requireNonNull(response.getBody()).getData().get("email"));
    }


    @Test
    @DisplayName("Handle Persistence And Data Integrity Exception")
    void testHandlePersistenceAndDataIntegrityException() {
        // Arrange
        PersistenceException persistenceException = new PersistenceException("Database error");
        DataIntegrityViolationException dataIntegrityException =
                new DataIntegrityViolationException("Integrity constraint violation");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/database");

        // Act
        ResponseEntity<GenericApiResponse<ErrorDetail>> responsePersistence =
                this.globalExceptionHandler.handlePerDataIntExcep(persistenceException, mockRequest);
        ResponseEntity<GenericApiResponse<ErrorDetail>> responseIntegrity =
                this.globalExceptionHandler.handlePerDataIntExcep(dataIntegrityException, mockRequest);

        // Assert
        Assertions.assertNotNull(responsePersistence);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responsePersistence.getStatusCode());
        Assertions.assertEquals("Une erreur technique est survenu", responsePersistence.getBody().getMessage());

        Assertions.assertNotNull(responseIntegrity);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseIntegrity.getStatusCode());
        Assertions.assertEquals("Une erreur technique est survenu", responseIntegrity.getBody().getMessage());
    }

    @Test
    @DisplayName("Handle Method Argument Not Valid Exception")
    void testHandleMethodArgumentNotValidException() {
        // Arrange
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/validateToTest");

        // Création d'un vrai BindingResult avec une erreur fictive
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objectName");
        bindingResult.addError(
                new FieldError("objectName", "fieldName", "Invalid field value"));

        // Création de l'exception avec le vrai BindingResult
        MethodArgumentNotValidException exceptionBindingResult = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Object> response =
                this.globalExceptionHandler.handleMethodArgumentNotValid(
                        exceptionBindingResult,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        new ServletWebRequest(mockRequest));


        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Handle Http Message Not Readable Exception")
    void testHandleHttpMessageNotReadableException() {
        // Arrange
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON request");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/json-error");
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        WebRequest request = new ServletWebRequest(mockRequest);

        // Act
        ResponseEntity<Object> response = globalExceptionHandler.handleHttpMessageNotReadable(ex, headers, status, request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
