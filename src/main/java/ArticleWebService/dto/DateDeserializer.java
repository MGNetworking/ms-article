package ArticleWebService.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Slf4j
public class DateDeserializer extends JsonDeserializer<Timestamp> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Timestamp deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {

        // récupération de la chain de caractère représentant la date
        String date = jsonParser.getText();
        try {
            // parse de la date et retour de la date au format Timestamp
            return new Timestamp(dateFormat.parse(date).getTime());
        } catch (ParseException e) {
            log.error("impossible d'analyser la date : " + e.getLocalizedMessage());
            throw new IOException("impossible d'analyser la date ", e);
        }
    }
}
