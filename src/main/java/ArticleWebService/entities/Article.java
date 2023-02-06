package ArticleWebService.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Embeddable
@Table(name = "article", schema = "ms_article")
public class Article implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_article")
    private Integer idArticle;

    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    @Column(name = "id_commentaire", nullable = true)
    private Integer idCommentaire;

    @Column(name = "id_source", nullable = true)
    private Integer idSource;

    @Column(name = "id_note", nullable = true)
    private Integer idNote;

    @Embedded
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_section", referencedColumnName = "id_section")
    @JsonBackReference
    private Section section = new Section();

    @Column(name = "titre", nullable = false)
    private String titre;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "article", nullable = false)
    private String article;

    @Column(name = "date_creation", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private Timestamp dateCreation;

    @Column(name = "date_maj", nullable = true)
    private Timestamp dateMaj;

    @Column(name = "vue", nullable = true)
    private int vue;

    @Column(name = "visibiliter", nullable = false)
    private boolean visibiliter;
}
