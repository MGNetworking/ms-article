package ArticleWebService.entities;

import ArticleWebService.dto.DateDeserializer;
import ArticleWebService.dto.DateSerialisation;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Cacheable(false) // déactive le cache de 2ᵉ niveau
@Table(name = "article", schema = "ms_article")
public class Article implements Serializable {

    // constructeur utilisé uniquement pour les tests unitaires
    public Article(Integer idArticle, Section section) {
        this.idArticle = idArticle;
        this.section = section;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_article")
    private Integer idArticle;

    //@ManyToOne(fetch = FetchType.EAGER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_section", referencedColumnName = "id_section")
    private Section section;

    @Column(name = "id_user", nullable = false)
    private String idUser;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "img_url", nullable = true)
    private String imgUrl;

    @Column(name = "imgdescription", nullable = true)
    private String imgDescription;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description_art", nullable = false)
    private String description;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "article", nullable = false)
    private String article;

    @Column(name = "visibiliter", nullable = false)
    private boolean isVisibale;

    @Column(name = "vue", nullable = true)
    private int vue;

    @Column(name = "portfolio", nullable = false)
    private boolean portfolio;

    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    @CreationTimestamp
    @Column(name = "date_creation", nullable = true)
    private Timestamp dateCreation;

    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    @Column(name = "date_maj", nullable = true)
    private Timestamp dateMaj;

}
