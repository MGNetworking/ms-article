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
    PORTFOLIO BOOLEAN NOT NULL,
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

-- Insertion des domaines
INSERT INTO MS_ARTICLE.DOMAIN (DESCRIPTION) VALUES
('INFORMATIQUE'),
('Crypto');

-- Insertion des sections
INSERT INTO MS_ARTICLE.SECTION (ID_DOMAIN, DESCRIPTION) VALUES
(1, 'Java'),
(1, 'Spring Boot'),
(1, 'JavaScript'),
(1, 'Angular'),
(1, 'Python'),
(2, 'Bitcoin');

-- Insertion des articles avec valeurs booléennes (TRUE/FALSE)
INSERT INTO MS_ARTICLE.ARTICLE (ID_SECTION, ID_USER, TITRE, ARTICLE, VISIBILITER, DESCRIPTION_ART, IMG_URL, PORTFOLIO) VALUES
-- Section Java (ID_SECTION = 1)
(1, 'user1', 'Les fondamentaux de Java', 'Cet article couvre les bases du langage Java.', TRUE, 'Introduction aux concepts de base de Java pour débutants', 'https://example.com/java-basics.png', TRUE),
(1, 'user2', 'Java 17 - Nouveautés', 'Découvrez les nouvelles fonctionnalités de Java 17.', FALSE, 'Analyse des améliorations apportées par Java 17', 'https://example.com/java17.png', TRUE),
(1, 'user3', 'Programmation concurrentielle en Java', 'Guide sur les threads et la concurrence.', TRUE, 'Maîtrisez la programmation multi-threads avec Java', 'https://example.com/java-concurrency.png', FALSE),
(1, 'user4', 'Collections en Java', 'Article détaillé sur les structures de données en Java.', FALSE, 'Tour d''horizon complet des collections Java', 'https://example.com/java-collections.png', FALSE),

-- Section Spring Boot (ID_SECTION = 2)
(2, 'user1', 'Débuter avec Spring Boot', 'Guide de démarrage pour Spring Boot.', TRUE, 'Premiers pas avec le framework Spring Boot', 'https://example.com/spring-boot-start.png', TRUE),
(2, 'user2', 'Microservices avec Spring Boot', 'Conception de microservices modernes.', FALSE, 'Architecture microservices basée sur Spring Boot', 'https://example.com/spring-microservices.png', TRUE),
(2, 'user3', 'Spring Security', 'Sécurisez vos applications Spring Boot.', TRUE, 'Implémentation de la sécurité dans vos applications', 'https://example.com/spring-security.png', FALSE),
(2, 'user4', 'Tests avec Spring Boot', 'Stratégies de test pour applications Spring Boot.', FALSE, 'Méthodologies de test unitaire et d''intégration', 'https://example.com/spring-testing.png', FALSE),

-- Section JavaScript (ID_SECTION = 3)
(3, 'user1', 'JavaScript moderne (ES6+)', 'Nouvelles fonctionnalités de JavaScript.', TRUE, 'Exploration des fonctionnalités modernes de JavaScript', 'https://example.com/modern-js.png', TRUE),
(3, 'user2', 'Asynchrone en JavaScript', 'Promises, Async/Await et plus.', FALSE, 'Maîtrisez la programmation asynchrone en JavaScript', 'https://example.com/async-js.png', TRUE),
(3, 'user3', 'TypeScript pour débutants', 'Introduction à TypeScript pour devs JS.', TRUE, 'Apprendre TypeScript quand on connaît JavaScript', 'https://example.com/typescript-intro.png', FALSE),
(3, 'user4', 'Node.js et Express', 'Développement backend avec JavaScript.', FALSE, 'Création d''APIs RESTful avec Node.js', 'https://example.com/nodejs-express.png', FALSE),

-- Section Angular (ID_SECTION = 4)
(4, 'user1', 'Débuter avec Angular', 'Les bases du framework Angular.', TRUE, 'Premier pas avec Angular pour développeurs frontend', 'https://example.com/angular-basics.png', TRUE),
(4, 'user2', 'State Management dans Angular', 'NgRx et gestion d''état avancée.', FALSE, 'Gérer l''état de votre application Angular efficacement', 'https://example.com/angular-state.png', TRUE),
(4, 'user3', 'Angular vs React', 'Comparaison des deux frameworks populaires.', TRUE, 'Analyse détaillée des différences entre Angular et React', 'https://example.com/angular-react.png', FALSE),
(4, 'user4', 'Tests unitaires avec Angular', 'Stratégies de test pour applications Angular.', FALSE, 'Meilleures pratiques pour tester votre code Angular', 'https://example.com/angular-testing.png', FALSE),

-- Section Python (ID_SECTION = 5)
(5, 'user1', 'Python pour débutants', 'Introduction au langage Python.', TRUE, 'Premiers pas avec Python, de zéro à héros', 'https://example.com/python-basics.png', TRUE),
(5, 'user2', 'Data Science avec Python', 'Pandas, NumPy et visualisation de données.', FALSE, 'Analyse de données avec les bibliothèques Python', 'https://example.com/python-datascience.png', TRUE),
(5, 'user3', 'Django Framework', 'Développement web avec Django.', TRUE, 'Créer des applications web robustes avec Django', 'https://example.com/django.png', FALSE),
(5, 'user4', 'Python pour l''IA', 'Introduction au Machine Learning avec Python.', FALSE, 'TensorFlow et PyTorch pour l''intelligence artificielle', 'https://example.com/python-ai.png', FALSE),

-- Section Bitcoin (ID_SECTION = 6)
(6, 'user1', 'Introduction au Bitcoin', 'Les fondamentaux de Bitcoin.', TRUE, 'Comprendre la première cryptomonnaie', 'https://example.com/bitcoin-intro.png', TRUE),
(6, 'user2', 'Analyse technique du Bitcoin', 'Étude des graphiques et tendances.', FALSE, 'Méthodes d''analyse pour prédire les mouvements du BTC', 'https://example.com/bitcoin-analysis.png', TRUE),
(6, 'user3', 'Sécurité et stockage de Bitcoin', 'Protection de vos investissements.', TRUE, 'Wallets et bonnes pratiques de sécurité', 'https://example.com/bitcoin-security.png', FALSE),
(6, 'user4', 'Bitcoin vs autres cryptomonnaies', 'Comparaison avec Ethereum, etc.', FALSE, 'Analyse comparative des principales cryptomonnaies', 'https://example.com/bitcoin-altcoins.png', FALSE);