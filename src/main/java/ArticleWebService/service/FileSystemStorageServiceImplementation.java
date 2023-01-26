package ArticleWebService.service;

import ArticleWebService.component.ConfigurationArticle;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Date;

/**
 * Cette classe permet l'enregistrement de fichier de type images sur le serveur locale
 */
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
    public String [] storeImage(MultipartFile file) throws Exception {

        try {

            String extention = FilenameUtils.getExtension(file.getOriginalFilename());
            String nameFile = FilenameUtils.getBaseName(file.getOriginalFilename());

            nameFile =  RandomString.make() + new Date().getTime() + "." + extention;
            log.info("remplacment de l'ancien nom de fichier par le nouveau");
            String  name = file.getOriginalFilename().replace(file.getOriginalFilename(),nameFile );

            String pathlocal = environment.getProperty("file.upload-dir");
            Path path = Paths.get(pathlocal + "/" + name);

            // recherche du dossier de reception
            if (!Files.exists(path.getParent())) {

                log.info("Le dossier n'existe pas et doit être créer ");
                File newDossier = new File(path.getParent().toString());

                if (newDossier.mkdir()){
                    log.info("dossier " + path.getFileName() + " a etait créer");
                }
            }

            log.info("Copie du fichier vers le serveur local");
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return new String[]{this.ipLocation + name, name };

        } catch (FileAlreadyExistsException e) {

            log.error("This file" + file.getOriginalFilename() + " already Exists");
            throw new IOException(e);

        } catch (IOException ioe) {

            log.error("Erreur File name : " + file.getOriginalFilename());
            throw new IOException(ioe);

        } catch (SecurityException se){

            log.error("Problème de creation de dossier : " + file.getOriginalFilename());
            throw new IOException(se);
        }

    }

    @Override
    public boolean deleteImages(String fileName) throws Exception {


        Path path = Paths.get(environment.getProperty("file.upload-dir") + "/" + fileName);
        try {
            log.info("Suppression de l'images : " + fileName);
            Files.delete(path);
            return true;
        } catch (IOException ioe) {
            throw new Exception(ioe);
        }

    }
}
