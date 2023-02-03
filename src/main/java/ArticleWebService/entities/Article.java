package ArticleWebService.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "article")
public class Article implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_article")
    private Long idArticle;

    @Column(name = "id_user", nullable = false)
    private Long idUser;

    @Column(name = "id_commentaire", nullable = true)
    private Long idCommentaire;

    @Column(name = "id_source", nullable = true)
    private Long idSource;

    @Column(name = "id_note", nullable = true)
    private Long idNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_section", referencedColumnName = "id_section")
    private Section section = new Section();

    @Column(name = "titre", nullable = false)
    private String titre;

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
