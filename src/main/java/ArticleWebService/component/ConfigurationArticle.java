package ArticleWebService.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.StreamSupport;


@Slf4j
@Component
public class ConfigurationArticle {

    @Autowired
    Environment environment;

    public Map<String, String> getProperties() {

        List<String> listProperties = new ArrayList<>();
        Map<String, String> param = new HashMap<>();

        log.info("StreamSupport de Profile ");
        StreamSupport.stream(((ConfigurableEnvironment) this.environment)
                        .getPropertySources()
                        .spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .forEach(propName -> {

                            log.info("{} : {}", propName, environment.getProperty(propName));
                            param.put(propName, environment.getProperty(propName));

                        }
                );

        return param;
    }


}
