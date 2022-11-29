package ArticleWebService.service;

import ArticleWebService.component.ConfigurationArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
@Slf4j
public class FileSystemStorageServiceImplementation implements FileSystemStorageService {


    @Autowired
    ConfigurationArticle configurationArticle;

    /**
     * Permet l'enregistrement d'un fichier de type images sur le serveur local
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public boolean store(MultipartFile file) throws Exception {
        try {


            if (file == null) {
                throw new Exception("File is null");
            }

            if (file.isEmpty()) {
                throw new Exception("File is empty ");
            }

            Path path = Paths.get(configurationArticle.getProperties().get("location path"));

            try (InputStream inputStream = file.getInputStream()) {

                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            }

        } catch (IOException ioe) {
            throw new Exception("Failed to store file " + file.getName() + ioe);
        }
        log.info(configurationArticle.getProperties().get("profile"));
        log.info(configurationArticle.getProperties().get("datasource url"));
        log.info(configurationArticle.getProperties().get("keycloak realm"));


        return false;
    }

    @Override
    public boolean delete(String fileName) {
        return false;
    }
}
