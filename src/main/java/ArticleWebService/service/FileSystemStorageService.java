package ArticleWebService.service;

import org.springframework.web.multipart.MultipartFile;


public interface FileSystemStorageService {

    String storeImage(MultipartFile file) throws Exception;

    boolean delete(String fileName);

}
