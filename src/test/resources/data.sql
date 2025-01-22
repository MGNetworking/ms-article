/*==============================================================*/
/* Nom de SGBD :  PostgreSQL 8                                  */
/* Date de création :  09/01/2023                               */
/* Mise à jour: 02/01/2025 - Intégration des champs manquants   */
/*==============================================================*/
BEGIN;

-- Création du schéma ms_article avec utilisateur
CREATE SCHEMA IF NOT EXISTS MS_ARTICLE;

DROP TABLE IF EXISTS MS_ARTICLE.ARTICLE CASCADE;
DROP TABLE IF EXISTS MS_ARTICLE.SECTION CASCADE;
DROP TABLE IF EXISTS MS_ARTICLE.DOMAIN CASCADE;



/*==============================================================*/
/* Table : DOMAIN                                               */
/*==============================================================*/
CREATE TABLE MS_ARTICLE.DOMAIN (
    ID_DOMAIN INTEGER AUTO_INCREMENT NOT NULL,
    DESCRIPTION VARCHAR(256) NOT NULL,
    CONSTRAINT PK_DOMAIN PRIMARY KEY (ID_DOMAIN)
);

/*==============================================================*/
/* Table : SECTION                                              */
/*==============================================================*/
CREATE TABLE MS_ARTICLE.SECTION (
    ID_SECTION INTEGER AUTO_INCREMENT NOT NULL,
    ID_DOMAIN INTEGER NOT NULL,
    DESCRIPTION VARCHAR(256) NOT NULL,
    CONSTRAINT PK_SECTION PRIMARY KEY (ID_SECTION)
);

/*==============================================================*/
/* Table : ARTICLE                                              */
/*==============================================================*/
CREATE TABLE MS_ARTICLE.ARTICLE (
    ID_ARTICLE INTEGER AUTO_INCREMENT NOT NULL,
    ID_USER VARCHAR(250) NOT NULL,
    ID_SECTION INTEGER NOT NULL,
    ID_SOURCE INTEGER NULL,
    ID_COMMENTAIRE INTEGER NULL,
    ID_NOTE INTEGER NULL,
    TITRE VARCHAR(100) NOT NULL,
    ARTICLE TEXT NOT NULL,
    DATE_CREATION TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    DATE_MAJ TIMESTAMP NULL,
    VUE INTEGER DEFAULT 0 NOT NULL,
    VISIBILITER BOOLEAN NOT NULL,
    IMGDESCRIPTION VARCHAR(250) NULL,
    DESCRIPTION_ART TEXT NOT NULL,
    IMG_URL VARCHAR(250) NULL,
    CONSTRAINT PK_ARTICLE primary key (ID_SECTION, ID_ARTICLE)
);

/*==============================================================*/
/* Gestion des clef  primaire et etrangers                      */
/*==============================================================*/
alter table MS_ARTICLE.ARTICLE add constraint FK_ARTICLE_SECTION foreign key (ID_SECTION) references MS_ARTICLE.SECTION (ID_SECTION) on delete restrict on update restrict;

alter table MS_ARTICLE.SECTION add constraint FK_SECTION_DOMAIN foreign key (ID_DOMAIN) references MS_ARTICLE.DOMAIN (ID_DOMAIN) on delete restrict on update restrict;

COMMIT;


/*==============================================================*/
/* Insertion de données pour les tests                          */
/*==============================================================*/
INSERT INTO MS_ARTICLE.DOMAIN (DESCRIPTION) VALUES
('INFORMATIQUE'),
('Crypto');

INSERT INTO MS_ARTICLE.SECTION (ID_DOMAIN, DESCRIPTION) VALUES
(1, 'Java'),
(1, 'Spring Boot'),
(1, 'JavaScript'),
(1, 'Angular'),
(1, 'Python'),
(2, 'Bitcoin');

INSERT INTO MS_ARTICLE.ARTICLE (ID_SECTION, ID_USER, TITRE, ARTICLE, VISIBILITER, DESCRIPTION_ART, IMG_URL) VALUES
(1, 'user1', 'Introduction à Java', 'Article sur Java.', TRUE, 'Introduction à Java', 'https://example.com/java.png'),
(2, 'user2', 'Spring Boot Basics', 'Article sur Spring Boot.', TRUE, 'Spring Boot pour les débutants', 'https://example.com/springboot.png');
