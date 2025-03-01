package ArticleWebService.repository;

import ArticleWebService.dto.ArticleDtoUpdate;
import ArticleWebService.entities.Article;
import ArticleWebService.projection.ArticleProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;


@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Query(value = "SELECT art FROM Article art WHERE art.section.idSection = :sect ORDER BY art.idArticle")
    Page<Article> findAllArticlesBySection(Pageable pageable, @Param("sect") Integer section);

    @Query(value = "SELECT art FROM Article art WHERE art.portfolio = false ORDER BY art.idArticle")
    Page<Article> findAllArticlePageOrderBy(Pageable pageable);

    // Retourne une page d'articles où portfolio est true (non-utiliser)
    //Page<ArticleProjection> findByPortfolioTrueOrderByIdArticleAsc(Pageable pageable);

    // Version avec projection dynamique
    <T> Page<T> findByPortfolioTrueOrderByIdArticleAsc(Pageable pageable, Class<T> type);

    /**
     * Permet de mettre à jour les champs d'article.
     *
     * @param dto l'objet de mise à jour.
     * @return Un entier pour le nombre de lignes affecté.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Article a SET " +
            "a.titre = COALESCE(:#{#dto.titre}, a.titre), " +
            "a.article = COALESCE(:#{#dto.article}, a.article), " +
            "a.imgUrl = COALESCE(:#{#dto.imgUrl}, a.imgUrl), " +
            "a.imgDescription = COALESCE(:#{#dto.imgDescription}, a.imgDescription), " +
            "a.description = COALESCE(:#{#dto.description}, a.description), " +
            "a.dateMaj = CURRENT_TIMESTAMP " +
            "WHERE a.section.idSection = :#{#dto.section.idSection} AND a.idArticle = :#{#dto.idArticle}")
    int updateArticleFields(@Param("dto") ArticleDtoUpdate dto);

    @Modifying
    @Transactional
    @Query("UPDATE Article a SET " +
            "a.vue = COALESCE(a.vue + :#{#dto.vue}, a.vue), " +
            "a.isVisibale = COALESCE(:#{#dto.isVisibale}, a.isVisibale) " +
            "WHERE a.section.idSection = :#{#dto.section.idSection} AND a.idArticle = :#{#dto.idArticle}")
    int updateArticleMeta(@Param("dto") ArticleDtoUpdate dto);


}
