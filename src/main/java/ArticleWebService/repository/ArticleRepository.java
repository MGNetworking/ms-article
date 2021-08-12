package ArticleWebService.repository;

import ArticleWebService.entities.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
@CrossOrigin(origins = "*")
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(value = "SELECT u from Article u")
    Optional<Article> findAllArticles(Pageable pageable);

}
