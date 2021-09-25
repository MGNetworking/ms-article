package ArticleWebService.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConfigurationArticle {

    @Autowired
    Environment environment;

    public Map<String,String> getProperties() {

        List<String> listProperties = new ArrayList<>();

        Map<String, String> param = new HashMap<>();

        param.put("name", environment.getProperty("spring.application.name"));
        param.put("profile", environment.getProperty("info.profile"));
        param.put("server port", environment.getProperty("server.port"));

        // logging
        param.put("path logging file", environment.getProperty("logging.file.path"));
        param.put("Level logging Controller Spring", environment.getProperty("logging.level.org.springframework.controller"));
        param.put("Level logging hibernate", environment.getProperty("logging.level.org.hibernate"));

        // actuator and devtool
        param.put("actuator management include", environment.getProperty("management.endpoints.web.exposure.include"));
        param.put("devtools restart enabled", environment.getProperty("spring.devtools.restart.enabled"));

        // data jpa
        param.put("datasource url", environment.getProperty("spring.datasource.url"));
        param.put("datasource username", environment.getProperty("spring.datasource.username"));
        if (!environment.getProperty("info.profile").equals("prod")){
            param.put("datasource password", environment.getProperty("spring.datasource.password"));
        }
        param.put("datasource driver-class-name", environment.getProperty("spring.datasource.driver-class-name"));

        param.put("jpa show-sql", environment.getProperty("spring.jpa.show-sql"));
        param.put("jpa properties hibernate format_sql", environment.getProperty("spring.jpa.properties.hibernate.format_sql"));

        // keyclaok
        param.put("keycloak realm", environment.getProperty("keycloak.realm"));
        param.put("keycloak cors", environment.getProperty("keycloak.cors"));
        param.put("keycloak resource", environment.getProperty("keycloak.resource"));
        param.put("keycloak auth-server-url", environment.getProperty("keycloak.auth-server-url"));
        param.put("keycloak public-client", environment.getProperty("keycloak.public-client"));

        // eureka
        param.put("eureka instance-id", environment.getProperty("eureka.instance.instance-id"));
        param.put("eureka client service-url defaultZone", environment.getProperty("eureka.client.service-url.defaultZone"));
        param.put("spring cloud discovery enabled", environment.getProperty("spring.cloud.discovery.enabled"));

        // serveur
        param.put("server forward-headers-strategy", environment.getProperty("server.forward-headers-strategy"));
        param.put("storage-article location", environment.getProperty("storage-article.location"));

        return param;
    }



}
