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

    /**
     * Retourne une pagination d'article par ordre croissant et déterminer par leur section
     *
     * @param section   Le numero identifiant de la section
     * @param visible   La visibilité des articles
     * @param portfolio La visibilité des articiles type portfolio
     * @param pageable  Objet qui contient le numero de page et le nombre d'éléments
     * @return La pagination des articles
     */
    @Query(value = "SELECT art FROM Article art WHERE art.section.idSection = :section and art.portfolio = :ptfolio AND art.visibiliter = :visible ORDER BY art.idArticle asc")
    Page<Article> findAllArticlesBySection(@Param("section") Integer section,
                                           @Param("visible") boolean visible,
                                           @Param("ptfolio") boolean portfolio,
                                           Pageable pageable);


    /**
     * Retourne une pagination d'article, avec un choix sur la visibilité des articles et des portfolios.
     * L'ordre dans lequel sont ordonnés les Articles reste à être déterminés par l'utilisateur.
     *
     * @param visible   La visibilité des articles
     * @param portfolio La visibilité des articiles type portfolio
     * @param pageable  Objet qui contient le numero de page et le nombre d'éléments
     * @return La pagination des articles
     */
    @Query("SELECT art FROM Article art WHERE art.portfolio = :ptfolio AND art.visibiliter = :visible ")
    Page<Article> findAllPortfolioArticlesByVisibility(@Param("visible") boolean visible,
                                                       @Param("ptfolio") boolean portfolio,
                                                       Pageable pageable);

    /**
     * Permet de retourner des paginations d'articles sous forme de projection dynamique.
     * Cette méthode récupère les articles ayant la propriété "portfolio" définie à true,
     * triés par ordre croissant de leur identifiant (idArticle).
     *
     * @param <T>      le type de la projection dynamique retournée, défini par l'appelant
     * @param pageable un objet {@link Pageable} contenant les informations de pagination,
     *                 telles que le numéro de la page et le nombre d'éléments par page
     * @param type     la classe de la projection dynamique à utiliser pour le mapping des résultats
     * @return une instance de {@link Page} contenant les articles paginés sous la forme de la projection spécifiée
     */
    <T> Page<T> findByPortfolioTrueOrderByIdArticleAsc(Pageable pageable, Class<T> type);

    /**
     * Permet de mettre à jour les champs d'article.
     *
     * @param dto l'objet Article de mise à jour.
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

    /**
     * Permet de mettre à jour les champs d'article concidérer comme méta données
     *
     * @param dto l'objet Article de mise à jour.
     * @return Un entier pour le nombre de lignes affecté.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Article a SET " +
            "a.vue = COALESCE(a.vue + :#{#dto.vue}, a.vue), " +
            "a.visibiliter = COALESCE(:#{#dto.visibiliter}, a.visibiliter), " +
            "a.portfolio = COALESCE(:#{#dto.portfolio}, a.portfolio) " +
            "WHERE a.idUser = :#{#dto.idUser} AND a.idArticle = :#{#dto.idArticle}")
    int updateArticleMeta(@Param("dto") ArticleDto dto);


}
