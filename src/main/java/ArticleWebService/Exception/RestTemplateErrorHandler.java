/*
package ArticleWebService.Exception;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

*/
/**
 * Gestion des exceptions générer par les accès au service externes (RestTemplate)
 *//*

@Component
public class RestTemplateErrorHandler implements ResponseErrorHandler {

    */
/**
     * Permet de déterminer si une erreur s'est produite lors de la communication avec le service externe
     *
     * @param clientHttpResponse the response to inspect
     * @return true si le status http de la réponse est une erreur client (4xx) ou (5xx)
     * @throws IOException
     *//*

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        HttpStatus statusCode = clientHttpResponse.getStatusCode();
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();

    }

    */
/**
     * Permet de traiter l'erreur en fonction du code de statut HTTP retourné par le service externe.
     *
     * @param clientHttpResponse the response with the error
     * @throws IOException
     *//*

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

        HttpStatus statusCode = clientHttpResponse.getStatusCode();
        String uri = clientHttpResponse.getHeaders().get("uri").get(0);

        if (statusCode.is4xxClientError()) {

            switch (clientHttpResponse.getStatusCode()) {

                // status 503
                case SERVICE_UNAVAILABLE:
                    throw new CustomerException(
                            clientHttpResponse.getStatusCode(),
                            clientHttpResponse.getStatusText(),
                            "Le service est actuellement indisponible",
                            uri);

                    // status 500
                case INTERNAL_SERVER_ERROR:
                    throw new CustomerException(
                            clientHttpResponse.getStatusCode(),
                            clientHttpResponse.getStatusText(),
                            "Une erreur interne c'est produite, veuillez contacter le service service concerné",
                            uri);


            }


        } else if (statusCode.is5xxServerError()) {

            switch (clientHttpResponse.getStatusCode()) {

                // Status 401
                case UNAUTHORIZED:
                    throw new CustomerException(
                            clientHttpResponse.getStatusCode(),
                            clientHttpResponse.getStatusText(),
                            "Accès non authoriser ",
                            uri);

                    // Status 404
                case NOT_FOUND:
                    throw new CustomerException(
                            clientHttpResponse.getStatusCode(),
                            clientHttpResponse.getStatusText(),
                            "Le service n'existe pas",
                            uri);

            }
        }

    }
}

*/
