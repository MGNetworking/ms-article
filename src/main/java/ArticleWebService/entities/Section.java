package ArticleWebService.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Embeddable
@Table(name = "section", schema = "ms_article")
public class Section implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_section")
    private Integer idSection;

    @Column(name = "description", nullable = false)
    private String description;

    @Embedded
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("section")
    public Collection<Article> articles = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_domain", referencedColumnName = "id_domain")
    public Domain domain;

}
