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
     * Allow to upload file picture in server local
     *
     * @param file MultipartFile picture
     * @return String l'IP adresse to file picture
     * @throws Exception
     */
    @Override
    public String storeImage(MultipartFile file) throws Exception {

        try {

            log.info("verification par son nom de l'existance du fichier");
            String pathlocal = environment.getProperty("file.upload-dir");
            Path path = Paths.get(pathlocal + "/" + file.getOriginalFilename());

            if (!Files.exists(path)) {
                log.info("le fichier " + file.getOriginalFilename() + " n'existe pas dans le repetoire : "
                        + pathlocal);
            } else {
                log.info("le fichier " + file.getOriginalFilename() + " existe dans le repetoire : " + pathlocal
                        + "et sera remplacé");
            }

            log.info("création du flux de lecture");
            InputStream inputStream = file.getInputStream();

            log.info("Copie du fichier vers le serveur local");
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

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
    public boolean deleteImages(String fileName) throws Exception {

        log.info("verification par son nom de l'existance du fichier");
        String pathlocal = environment.getProperty("file.upload-dir");
        Path path = Paths.get(pathlocal + "/" + fileName);


        try {
            Files.delete(path);
            return true;

        } catch (IOException ioe) {

            throw new Exception(ioe);
        }

    }
}
