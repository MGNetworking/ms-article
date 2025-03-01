package ArticleWebService.projection;

import ArticleWebService.entities.Section;

public interface ArticleProjection {

    Integer getIdArticle();

    Section getSection();

    String getTitre();

    String getImgUrl();

    String getImgDescription();

    String getDescription();

    boolean getIsVisibale();

    boolean getPortfolio();

}
