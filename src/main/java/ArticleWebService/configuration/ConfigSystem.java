package ArticleWebService.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.File;

@Slf4j
@Configuration
public class ConfigSystem {

    @Autowired
    Environment environment;

    String path;

    /**
     * Au démarrage de l'application initialise les chemins d'accès et verifies les
     * répertoires.
     *
     * @throws Exception en cas d'erreur d'initialisation.
     */
    @PostConstruct
    public void init() throws Exception {

        this.getEnv(); // init path en fonction de l'OS
        this.initFolderOs(); // Vérifie les dossiers


    }

    /**
     * Vérifie l'existence du répertoire et le créer s'il n'existe pas.
     */
    public void initFolderOs() {

        File folder = new File(this.path);

        if (!folder.exists()) {

            if (folder.mkdirs()) {
                log.error("Dossier créé avec succès !");
            } else {
                log.error("Erreur lors de la création du dossier ");
            }

        } else {
            log.error("Le dossier existe déjà.");
        }


    }


    /**
     * Recherche le type d'OS en cours d'exécution et initialise les chemins d'accès en correspondance
     * avec le système Host
     *
     * @throws Exception en cas d'erreur liait à l'analyse du système Host.
     */
    public void getEnv() throws Exception {

        // récupère le nom du system d'exploitation
        String osName = System.getProperty("os.name").toLowerCase();


        // filtre en fonction du système
        if (osName.contains("win")) {

            // permet de voir toutes les variables disponibles sur windows
            System.getenv().forEach((k, v) -> {
                log.info(k + ":" + v);
            });

            this.path = System.getenv("PUBLIC") + this.environment.getProperty("file.upload-dir-windows");
            log.info("Path config windows : " + path);

        } else if (osName.contains("nux") || osName.contains("nix") || osName.contains("aix")) {

            // permet de voir toutes les variables disponibles sur ubuntu
            System.getenv().forEach((k, v) -> {
                log.info(k + ":" + v);
            });

            this.path = System.getenv("HOME") + this.environment.getProperty("file.upload-dir-ubuntu");
            log.info("Path config linux : " + path);

        } else {
            log.error("Une erreur est survenu pendant l'analyse de l'OS ");
            throw new Exception("Erreur : un problème est survenu pendant la recherche du type d'OS " +
                    "du système d'exploitation ");
        }

    }

    /**
     * @return le chemin d'accès sur le système Host.
     */
    public String getPath() {
        return this.path;
    }

}
