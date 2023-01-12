package ArticleWebService.service;

import ArticleWebService.component.ConfigurationArticle;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
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
import java.util.Date;


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

            // recuperation du type de contenu
            String formatContentType = file.getContentType();
            log.info("URL formatContentType : " + formatContentType);

            // soustraction du type de contenu
            int indexContentType = formatContentType.lastIndexOf("/");
            // -1 si pas de symbole trouver
            if (indexContentType != -1) {
                formatContentType = formatContentType.substring(indexContentType + 1);
                log.info("ContentType image : " + formatContentType);
            }

            // creation du nouveau nom du fichier

            log.info("création du flux de lecture");
            InputStream inputStream = file.getInputStream();

            log.info("Content type : " + file.getContentType());

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

    /**
     * Permet la sauvegarde d'une images via une adresse IP.
     * telechargement de l'images et transforme en fichier de type multipartFile
     * dans le but de sa sauvegarde
     *
     * @param file
     * @return l'adresse générer
     * @throws Exception
     */
    @Override
    public String storeImageWithURL(String urlImages) throws IOException, Exception {

        // TODO Certainne ne fonction pas GIF
        URI uri = URI.create(urlImages);
        HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL().openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0");
        urlConnection.setReadTimeout(5000);
        urlConnection.setConnectTimeout(5000);

        log.info("getRequestProperties" + urlConnection.getRequestProperties().toString());
        log.info("Autority is : " + uri.getRawAuthority());
        log.info("URI : " + uri.parseServerAuthority());

        try ( InputStream input = uri.toURL().openStream()) {

            // recupération du type concernant l'images
            URL url = new URL(urlImages);
            String formatContentType = url.openConnection().getContentType();
            log.info("ContentType : " + formatContentType);

            int indexContentType = formatContentType.lastIndexOf("/");

            // -1 si pas de symbole trouver
            if (indexContentType != -1) {
                formatContentType = formatContentType.substring(indexContentType + 1);
                log.info("Type image : " + formatContentType);
            }

            // écriture de l'images sur le serveur
            String fileName = RandomString.make() + new Date().getTime() + "." + formatContentType;
            log.info("Nom du fichier : " + fileName);

            Path path = Paths.get(environment.getProperty("file.upload-dir") + "/" + fileName);

            log.info("Copie du fichier vers dans : " + path);
            Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);

            return this.ipLocation + fileName ;

        } catch (IOException | IllegalArgumentException | IndexOutOfBoundsException ioe) {

            log.error(ioe.getMessage());
            log.error(ioe.getStackTrace().toString());

            throw new IOException(ioe);

        } catch (Exception ex) {

            log.error(ex.getMessage());
            throw new Exception(ex);
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
