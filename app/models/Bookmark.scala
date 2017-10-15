package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

import scala.language.postfixOps
import play.api.Logger

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
    * Retrieve a employee from the id.
    */
  def findById(id: Long): Option[Bookmark] = {
    DB.withConnection { implicit connection =>
      SQL("select * from bookmark where id = {id}").on('id -> id).as(bookmark.singleOpt)
    }
  }

  def findBySlug(slug: String): Option[Bookmark] = {
    DB.withConnection { implicit connection =>
      SQL("select * from bookmark where slug = {slug}").on('slug -> slug).as(bookmark.singleOpt)
    }
  }

  def findByUrl(url: String): Option[Bookmark] = {
    DB.withConnection { implicit connection =>
      SQL("select * from bookmark where url = {url}").on('url-> url).as(bookmark.singleOpt)
    }
  }
//  /**
//    * Return a page of (Bookmarks).
//    *
//    * @param page Page to display
//    * @param pageSize Number of employees per page
//    * @param orderBy Employee property used for sorting
//    * @param filter Filter applied on the name column
//    */
//  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[Bookmark] = {
//
//    val offest = pageSize * page
//
//    DB.withConnection { implicit connection =>
//
//      val bookmarks = SQL(
//        """
//          select * from bookmark
//          where bookmark.name like {filter}
//          order by {orderBy} nulls last
//          limit {pageSize} offset {offset}
//        """).on(
//        'pageSize -> pageSize,
//        'offset -> offest,
//        'filter -> filter,
//        'orderBy -> orderBy).as(bookmark *)
//
//      val totalRows = SQL(
//        """
//          select count(*) from bookmark
//          where bookmark.name like {filter}
//        """).on(
//        'filter -> filter).as(scalar[Long].single)
//
//      Page(bookmarks, page, offest, totalRows)
//
//    }
//
//  }

  /**
    * Retrieve all Bookmarks by token
    *
    * @return
    */
  def findAll(token: String): List[Bookmark] = {
    DB.withConnection { implicit connection =>
      try {
        SQL("select b.* from bookmark b join token k on(b.userId = k.userId and k.token = {token}) order by name")
          .on('token -> token)
          .as(bookmark *)
      } catch {
        case ex: Exception => Logger.info("ERROR", ex); Nil
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

  override def toString = super.toString

  val tokenp = {
    get[Long]("token.userId") ~
      get[String]("token.token")  map {
      case id ~ token => Token(id, token)
    }
  }

  /**
    * Insert a new bookmark.
    *
    */
  def insert(bookmark: Bookmark, slug: String, token: String): Option[Long] = {
    DB.withConnection { implicit connection =>
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
          'userId -> SQL("select * from token t where t.token = {token}").on('token -> token).as(tokenp.single).id
        ).executeInsert()
    }
  }

  /**
    * Delete a Bookmark.
    *
    * @param id Id of the employee to delete.
    */
  def delete(id: Long): Int = {
    DB.withConnection { implicit connection =>
      SQL("delete from bookmark where id = {id}").on('id -> id).executeUpdate()
    }
  }

}
