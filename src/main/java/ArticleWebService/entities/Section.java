package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@Table(name = "section")
public class Section implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_section")
    private Long idSection;

    @Column(name = "description", nullable = false)
    private Long description;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    public Collection<Article> articles = new ArrayList<>();

    @ManyToOne
    public Domain domain;

}
