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
@Table(name = "article", schema = "ms_article")
public class Article implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_article")
    private Integer idArticle;

    @Column(name = "id_user", nullable = false)
    private String idUser;

    @Column(name = "id_commentaire", nullable = true)
    private Integer idCommentaire;

    @Column(name = "id_source", nullable = true)
    private Integer idSource;

    @Column(name = "id_note", nullable = true)
    private Integer idNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_section", referencedColumnName = "id_section")
    //@JsonBackReference
    private Section section;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "article", nullable = false)
    private String article;


    @Column(name = "imgdescription", nullable = true)
    private String imgDescription;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description_art", nullable = false)
    private String description;

    //@Column(name = "date_creation", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    @CreationTimestamp
    @Column(name = "date_creation", nullable = true)
    private Timestamp dateCreation;

    @JsonSerialize(using = DateSerialisation.class)
    @JsonDeserialize(using = DateDeserializer.class)
    @UpdateTimestamp
    @Column(name = "date_maj", nullable = true)
    private Timestamp dateMaj;

    @Column(name = "vue", nullable = true)
    private int vue;

    @Column(name = "visibiliter", nullable = false)
    private boolean visibiliter;
}
