package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "domain", schema = "ms_article")
public class Domain implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_domain")
    public Integer idDomain;

    @Column(name = "description")
    public String description;

    @OneToMany(mappedBy = "domain", fetch = FetchType.EAGER)
    public Collection<Section> sections;
}
