package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "domain")
public class Domain implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_domain")
    public Long idDomain;

    @Column(name = "description")
    public String description;

    @OneToMany(mappedBy = "domain" ,fetch = FetchType.EAGER)
    public Collection<Section> sections;
}
