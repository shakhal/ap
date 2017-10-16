# --- First database schema

# --- !Ups

CREATE SEQUENCE bookmark_seq;

CREATE TABLE bookmark (
  id                        BIGINT NOT NULL DEFAULT NEXTVAL ('bookmark_seq'),
  name                      VARCHAR(255) NOT NULL,
  url                       VARCHAR(1000) NOT NULL,
  slug                      VARCHAR(255) NOT NULL,
  userId                    BIGINT NOT NULL,
  CONSTRAINT pk_bookmark PRIMARY KEY (id),
  CONSTRAINT uq_userid_url UNIQUE (userId,url)
);

CREATE INDEX on slug_idx bookmark(slug);
CREATE INDEX on url_idx bookmark(url);

CREATE SEQUENCE user_seq;

CREATE TABLE users (
  userId                    BIGINT NOT NULL DEFAULT NEXTVAL ('user_seq'),
  email                     VARCHAR(255) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (userId)
);

CREATE INDEX email_idx on users(email);

CREATE TABLE token (
  userId                    BIGINT NOT NULL,
  token                     VARCHAR(255) NOT NULL,
  createdAt                 DATE,
  CONSTRAINT pk_token PRIMARY KEY (token)
)
;

# --- !Downs

drop table if exists bookmark;
drop table if exists users;
drop table if exists token;
