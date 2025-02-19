package ArticleWebService.integration;

import ArticleWebService.SystemPropertiesActiveProfileResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de charge pour vérifier si l'application gère correctement un grand nombre de requêtes simultanées
 * sans provoquer de blocage de threads. Il mesure également les temps de réponse et les erreurs éventuelles.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(resolver = SystemPropertiesActiveProfileResolver.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ThreadBlockingITTest {

    /**
     * Client REST utilisé pour envoyer des requêtes HTTP à l'application testée.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Teste le comportement de l'application sous forte charge en simulant 50 requêtes concurrentes.
     * Vérifie si l'application répond sans blocage et si toutes les réponses ont un statut HTTP 200.
     * Mesure également les temps de réponse.
     *
     * @throws InterruptedException si une interruption du thread principal survient pendant l'attente des résultats.
     */
    @Test
    @DisplayName("Test de charge : vérification de la gestion des requêtes simultanées et des temps de réponse")
    public void testThreadBlockingUnderLoad() throws InterruptedException {
        int numThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        ExecutorCompletionService<Long> completionService = new ExecutorCompletionService<>(executor);
        List<Long> responseTimes = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            completionService.submit(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    ResponseEntity<String> response = restTemplate.getForEntity("/article/getArticle/1", String.class);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                    return System.currentTimeMillis() - startTime;  // Mesurer en millisecondes
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    return -1L;
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);

        for (int i = 0; i < numThreads; i++) {
            try {
                Future<Long> future = completionService.poll(10, TimeUnit.SECONDS);
                if (future != null) {
                    long responseTime = future.get();
                    if (responseTime != -1) {
                        responseTimes.add(responseTime);
                    }
                }
            } catch (ExecutionException e) {
                System.err.println("Execution Exception: " + e.getMessage());
                errorCount.incrementAndGet();
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Assertions avec messages explicites
        assertThat(successCount.get())
                .withFailMessage("Aucune requête réussie.")
                .isGreaterThan(0);

        assertThat(errorCount.get())
                .withFailMessage("Trop de requêtes échouées.")
                .isLessThanOrEqualTo(5);

        assertThat(responseTimes)
                .withFailMessage("Les temps de réponse ne sont pas enregistrés.")
                .isNotEmpty();

        assertThat(responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0))
                .withFailMessage("Le temps de réponse moyen est trop élevé.")
                .isLessThan(2000);

        System.out.println("Nombre de requêtes réussies : " + successCount.get());
        System.out.println("Nombre de requêtes échouées : " + errorCount.get());
        System.out.println("Temps de réponse moyen : " + responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0) + " ms");

        // Vérification de l'état des threads avec Actuator
        System.out.println("Vérification de l'état des threads avec Actuator...");
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/threaddump", String.class);
        assertThat(response.getStatusCode())
                .withFailMessage("L'endpoint /actuator/threaddump a échoué.")
                .isEqualTo(HttpStatus.OK);
        System.out.println("Résultat du thread dump :");
        System.out.println(response.getBody());
    }


}
