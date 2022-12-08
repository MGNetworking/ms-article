package ArticleWebService.service;

import ArticleWebService.component.ConfigurationArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;


@Service
@Slf4j
public class FileSystemStorageServiceImplementation implements FileSystemStorageService {


    @Autowired
    Environment environment;

    @Value("${file.domain-dir}")
    private String ipLocation;

    /**
     * Permet l'enregistrement d'un fichier de type images sur le serveur local
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public String storeImage(MultipartFile file) throws Exception {

        try {
            if (file == null) {
                log.error("File is null");
                throw new Exception("File is null");
            }

            if (file.isEmpty()) {
                log.error("File is empty");
                throw new Exception("File is empty ");
            }

            log.info("verification par son nom de l'existance du fichier");
            Path path = Paths.get(environment.getProperty("file.upload-dir") + file.getName());

            if (!Files.exists(path)) {

                path = Paths.get(environment.getProperty("file.upload-dir"));
                log.info("le fichier " + file.getName() + "n'existe pas dans le repetoire : " + path);
            }

            log.info("création du flux de lecture");
            InputStream inputStream = file.getInputStream();

            log.info("Copie du fichier vers le serveur local");
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            // retour de l'adress ip de l'images
            return this.ipLocation + file.getOriginalFilename();

        } catch (FileAlreadyExistsException e) {

            log.error("This file" + file.getOriginalFilename() + " already Exists");
            throw new IOException(e);

        } catch (IOException ioe) {

            log.error("Erreur File name : " + file.getOriginalFilename());
            throw new IOException(ioe);
        }

    }

    @Override
    public boolean delete(String fileName) {
        return false;
    }
}
