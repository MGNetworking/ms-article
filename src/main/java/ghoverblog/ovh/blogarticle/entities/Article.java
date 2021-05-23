package ghoverblog.ovh.blogarticle.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @ToString
@Table(name = "article")
public class Article {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;

    @Column(name = "user_id",nullable = false)
    private String userId;

    @Column(name = "titre",nullable = false)
    private String titre;

    @Column(name = "texte",nullable = false)
    private String texte;

    @Column(name = "date_creation",nullable = false)
    private Date date;

    @Column(name = "path_image",nullable = false)
    private String path;

}
