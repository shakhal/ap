# --- First database schema

# --- !Ups

CREATE TABLE bookmark (
  id                        BIGINT NOT NULL AUTO_INCREMENT,
  name                      VARCHAR(255) NOT NULL,
  url                       VARCHAR(1000) NOT NULL,
  slug                      VARCHAR(255) NOT NULL,
  userId                    BIGINT NOT NULL,
  CONSTRAINT pk_bookmark PRIMARY KEY (id),
  CONSTRAINT uq_userid_url UNIQUE (userId,url),
  INDEX (slug),
  INDEX (url)
)
;

CREATE TABLE user (
  userId                    BIGINT NOT NULL AUTO_INCREMENT,
  email                     VARCHAR(255) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (userId),
  INDEX (email)
);

CREATE TABLE token (
  userId                    BIGINT NOT NULL,
  token                     VARCHAR(255) NOT NULL,
  createdAt                 DATE,
  CONSTRAINT pk_token PRIMARY KEY (token)
)
;
# --- !Downs

drop table if exists bookmark;
drop table if exists user;
drop table if exists token;
