package ArticleWebService.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Cette class permet la serialisation des l'objets courants
 */
@Slf4j
public class DateSerialisation extends JsonSerializer {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(Object objet,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {

        log.info("Demande Serialisation d'objet : {}", objet.toString());

        if (objet instanceof Timestamp) {

            try {

                log.info("Serialisation de l'objet : {}", objet.toString());
                String formatDate = dateFormat.format(objet);
                jsonGenerator.writeString(formatDate);

            } catch (IllegalArgumentException e) {

                log.error("impossible d'analyser la date : {}", e.getLocalizedMessage());
                throw new IOException("impossible d'analyser la date ", e);

            }

        }

    }


}
