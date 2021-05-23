package ghoverblog.ovh.blogarticle;

import ghoverblog.ovh.blogarticle.entities.Article;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class add {

    public List<Article> articleList = new ArrayList<>();

    public add() {

        articleList.add(article1);
        articleList.add(article2);
        articleList.add(article3);
        articleList.add(article4);
        articleList.add(article5);
        articleList.add(article6);
        articleList.add(article7);
        articleList.add(article8);
        articleList.add(article9);

    }

    public List<Article> getList(){
        return this.articleList;
    }

    Article article1 = new Article(1L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "Fini la formation",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/101.jpg");

    Article article2 = new Article(2L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "en recherche d'emplois",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/202.jpg");

    Article article3 = new Article(3L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "Les technologies Java",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/303.jpg");

    Article article4 = new Article(4L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "Les technologies Java",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/404.jpg");

    Article article5 = new Article(5L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "Les technologies Java",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/505.jpg");

    Article article6 = new Article(6L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "Les technologies Java",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/606.jpg");

    Article article7 = new Article(7L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "L'IA",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/707.jpg");

    Article article8 = new Article(8L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "Spring boot",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/808.jpg");


    Article article9 = new Article(9L,
            "e1853619-5f46-4f3b-930e-5770765818e2",
            "Le frame Angulars",
            "Est ipsum sint officia quis nulla nisi cupidatat aliquip nisi laboris " +
                    "eiusmod eiusmod aliquip do. Commodo elit excepteur occaecat irure Lorem " +
                    "sit nulla nulla sint duis incididunt. Nostrud ut do mollit et amet velit " +
                    "aute excepteur et culpa culpa velit. Cillum veniam officia anim cupidatat.",
            new Date(),
            "static/blog/909.jpg");



}
