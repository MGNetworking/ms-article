package ArticleWebService.repository;

import ArticleWebService.dto.ArticleDto;
import ArticleWebService.entities.Article;
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
            "a.titre = CASE WHEN :#{#dto.titre} IS NULL THEN a.titre ELSE :#{#dto.titre} END, " +
            "a.article = CASE WHEN :#{#dto.article} IS NULL THEN a.article ELSE :#{#dto.article} END, " +
            "a.imgUrl = CASE WHEN :#{#dto.imgUrl} IS NULL THEN a.imgUrl ELSE :#{#dto.imgUrl} END, " +
            "a.imgDescription = CASE WHEN :#{#dto.imgDescription} IS NULL THEN a.imgDescription ELSE :#{#dto.imgDescription} END, " +
            "a.description = CASE WHEN :#{#dto.description} IS NULL THEN a.description ELSE :#{#dto.description} END, " +
            "a.dateMaj = CURRENT_TIMESTAMP " +
            "WHERE a.idUser = :#{#dto.idUser} AND a.idArticle = :#{#dto.idArticle}")
    int updateArticleFields(@Param("dto") ArticleDto dto);

    @Modifying
    @Transactional
    @Query("UPDATE Article a SET " +
            "a.vue = COALESCE(a.vue + :#{#dto.vue}, a.vue), " +
            "a.isVisibale = COALESCE(:#{#dto.isVisibale}, a.isVisibale), " +
            "a.portfolio = COALESCE(:#{#dto.portfolio}, a.portfolio) " +
            "WHERE a.idUser = :#{#dto.idUser} AND a.idArticle = :#{#dto.idArticle}")
    int updateArticleMeta(@Param("dto") ArticleDto dto);


}
