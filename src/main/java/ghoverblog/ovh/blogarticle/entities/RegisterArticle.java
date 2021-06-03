package ghoverblog.ovh.blogarticle.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterArticle {

    private String userId;

    private String titre;

    private String texte;

    private Date date;

    private String path;

    private String hrefArticle;
}
