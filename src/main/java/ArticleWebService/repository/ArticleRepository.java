package ArticleWebService.repository;

import ArticleWebService.entities.Article;
import com.jayway.jsonpath.Option;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.data.jpa.repository.Query;

@Repository
@CrossOrigin(origins = "*")
public interface ArticleRepository extends PagingAndSortingRepository<Article, Long> {
}
