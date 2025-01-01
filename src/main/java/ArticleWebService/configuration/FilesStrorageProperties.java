package ArticleWebService.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Classe de configuration permet d'accéder au propriété
 */
@ConfigurationProperties(prefix = "file")
public class FilesStrorageProperties {

    private String uploadDir;

    public String getUploadDir(){
        return this.uploadDir;
    }

    public void setUploadDir(String uploadDir){
        this.uploadDir = uploadDir;
    }
}
