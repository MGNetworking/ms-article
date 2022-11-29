package ArticleWebService.service;

import org.springframework.web.multipart.MultipartFile;


public interface FileSystemStorageService {

    boolean store(MultipartFile file) throws Exception;

    boolean delete(String fileName);

}
