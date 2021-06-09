package ghoverblog.ovh.blogarticle.repository;

import ghoverblog.ovh.blogarticle.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource
@CrossOrigin(origins = "*")
public interface ArticleRepository extends JpaRepository<Article, Long> {

/*
    @RestResource(path = "/searchArticleTitle")
    @Query("select art from Article art  where art.name like :x")
    public List<Article> searchArticleTitle(@Param("x") String x);
 */
}
