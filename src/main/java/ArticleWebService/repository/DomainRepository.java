package ArticleWebService.repository;

import ArticleWebService.entities.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DomainRepository extends CrudRepository<Domain, Integer> {

/*    @Query("SELECT d FROM Domain d JOIN FETCH d.sections")
    List<Domain> findAllWithSections();*/
}
