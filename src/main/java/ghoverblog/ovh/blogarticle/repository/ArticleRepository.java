package ghoverblog.ovh.blogarticle.repository;

import ghoverblog.ovh.blogarticle.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@RepositoryRestResource
@CrossOrigin(origins = "*")
//@CrossOrigin("http://ghoverblog")
//@CrossOrigin( origins = "http://localhost/*")
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
