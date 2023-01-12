package ArticleWebService.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface FileSystemStorageService {

    String storeImage(MultipartFile file) throws Exception;

    String storeImageWithURL(String urlImages) throws Exception;

    boolean deleteImages(String fileName)throws Exception;

}
