package models

import anorm.SqlParser._
import anorm._
import play.api.Logger
import play.api.Play.current
import play.api.db._

import scala.language.postfixOps

case class Bookmark(id: Long, name: String, url: String, slug: String)

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}


object Bookmark {

  // -- Parsers

  /**
    * Parse a Bookmark from a ResultSet
    */
  val bookmark = {
    get[Long]("bookmark.id") ~
      get[String]("bookmark.name") ~
      get[String]("bookmark.url") ~
      get[String]("bookmark.slug") map {
      case id ~ name ~ url ~ slug => Bookmark(id, name, url, slug)
    }
  }

  // -- Queries
  Bookmark

  /**
    * Get Bookmark by id.
    */
  def findById(id: Long, token: String): Option[Bookmark] = {
    DB.withConnection { implicit connection =>
      try {
        SQL("select * from bookmark where id = {id} and userId = (select userId from token where token={token})")
          .on(
            'id -> id,
            'token -> token
          ).as(bookmark.singleOpt)
      }
      catch {
        case ex: Exception => Logger.info("ERROR", ex);
          None
      }
    }
  }

  /**
    * Get Bookmark by slug
    */
  def findBySlug(slug: String): Option[Bookmark] = {
    DB.withConnection { implicit connection =>
      SQL("select * from bookmark where slug = {slug}").on('slug -> slug).as(bookmark.singleOpt)
    }
  }

  /**
    * Get Bookmark by URL
    */
  def findByUrl(url: String): Option[Bookmark] = {
    DB.withConnection { implicit connection =>
      SQL("select * from bookmark where url = {url} limit 1").on('url-> url).as(bookmark.singleOpt)
    }
  }

  /**
    * Retrieve all Bookmarks by user token
    */
  def findAll(token: String): List[Bookmark] = {
    DB.withConnection { implicit connection =>
      try {
        SQL("select b.* from bookmark b join token k on(b.userId = k.userId and k.token = {token}) order by name")
          .on('token -> token)
          .as(bookmark *)
      } catch {
        case ex: Exception => Logger.info("ERROR", ex);
          Nil
      }
    }
  }

  /**
    * Update a bookmark.
    *
    * @param id The bookmark id
    * @param bookmark The bookmark values.
    */
  def update(id: Long, bookmark: Bookmark): Int = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update bookmark
          set name = {name}, url = {url}, slug = {slug}
          where id = {id}
        """).on(
        'id -> id,
        'name -> bookmark.name,
        'url -> bookmark.url,
        'slug -> bookmark.slug).executeUpdate()
    }
  }

  /**
    * Insert a new bookmark.
    *
    */
  def insert(bookmark: Bookmark, slug: String, token: String): Option[Long] = {
    DB.withConnection { implicit connection =>
      try {
        SQL(
          """
          insert into bookmark values (
    		 {id}, {name}, {url}, {slug}, {userId}
          )
        """).on(
          'id -> Option.empty[Long],
          'name -> bookmark.name,
          'url -> bookmark.url,
          'slug -> slug,
          'userId -> SQL("select * from token t where t.token = {token}").on('token -> token).as(User.token.single).userId
        ).executeInsert()
      }
      catch {
        case e: Exception => println("exception caught: " + e);
        None
      }
    }
  }

  /**
    * Delete a Bookmark.
    *
    * @param id Id of the employee to delete.
    */
    def delete(id: Long, token: String ): Int = {
      DB.withConnection { implicit connection =>
        SQL("delete from bookmark where id={id} and userId = (select userId from token where token={token})")
          .on(
            'id -> id,
            'token -> token
          )
          .executeUpdate()
    }
  }

}
