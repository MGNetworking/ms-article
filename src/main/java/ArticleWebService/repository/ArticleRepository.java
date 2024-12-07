package ArticleWebService.repository;

import ArticleWebService.entities.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Query(value = "SELECT art FROM Article art WHERE art.section.idSection = :sect ORDER BY art.idArticle")
    Page<Article> findAllArticlesBySection(Pageable pageable, @Param("sect") Integer section);

    @Query(value = "SELECT art FROM Article art ORDER BY art.idArticle")
    Page<Article> findAllArticlePageOrderBy(Pageable pageable);

}
