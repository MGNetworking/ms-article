package ArticleWebService.feign;

import ArticleWebService.entities.FileResponseClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "STORE-SERVICE")
public interface StorageRestClient {

    @PostMapping("/upload-image")
    @ResponseBody
    FileResponseClient uploadingImage(@RequestParam("image")MultipartFile file);

    @PostMapping("/upload-multiple-image")
    @ResponseBody
    List<FileResponseClient> uploadingMultipleImages(@RequestParam("images")MultipartFile[] file);
}
