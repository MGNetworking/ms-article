package ArticleWebService.repository;

import ArticleWebService.entities.Article;
import ArticleWebService.entities.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends PagingAndSortingRepository<Article, Integer> {


    @Query(value = "SELECT * FROM article art WHERE art.id_section =:section ORDER BY art.id_article",
    nativeQuery = true)
    Page<Article> findAllArticlesBySection(  Pageable pageable, @Param("section") Integer section);

    List<Article> findBySectionIdSection(Integer section);

/*
    @Query(value = "SELECT art FROM article art " +
            "LEFT JOIN FETCH art.section sect WHERE sect.id_section =:section ORDER BY art.id_article",
            nativeQuery = true)
    Page<Article> findAllArticlesBySection(Pageable pageable, @Param("section") int section);
*/



}
