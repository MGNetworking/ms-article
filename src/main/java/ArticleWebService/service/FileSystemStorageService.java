package ArticleWebService.service;

import org.springframework.web.multipart.MultipartFile;


public interface FileSystemStorageService {

    String storeImage(MultipartFile file) throws Exception;

    boolean deleteImages(String fileName)throws Exception;

}
