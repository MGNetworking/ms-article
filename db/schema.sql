------------------------------------------------------
-- create to schema  for blog
------------------------------------------------------
DROP SCHEMA IF EXISTS article_MS CASCADE;
CREATE SCHEMA article_MS AUTHORIZATION max_admin;
COMMENT ON SCHEMA article_MS IS 'schema de gestion des articles';

DROP TABLE IF EXISTS article_MS.article CASCADE;
------------------------------------------------------
-- article
------------------------------------------------------
CREATE TABLE article_MS.article
(
    article_id    serial primary key,
    user_id       varchar(100) NOT NULL,
    titre         char(50)     NOT NULL,
    texte         text         NOT NULL,
    date_creation date         NOT NULL,
    path_image    varchar(150) NOT NULL


);
