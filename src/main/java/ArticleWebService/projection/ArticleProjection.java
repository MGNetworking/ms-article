package ArticleWebService.projection;

import ArticleWebService.entities.Section;

public interface ArticleProjection {

    Integer getIdArticle();

    String getIdUser();

    Section getSection();

    String getTitre();

    String getImgUrl();

    String getImgDescription();

    String getDescription();

    boolean getVisibiliter();

    boolean getPortfolio();

}
