package models

import java.util.Date
import java.util.Calendar

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

import scala.language.postfixOps
import play.api.Logger

case class User(id: Long, email: String)
case class Token(token: String, userId: Long, createdAt: Date)


object User {

  // -- Parsers

  /**
    * Parse a user from a ResultSet
    */
  val user = {
    get[Long]("user.userId") ~
      get[String]("user.email")  map {
      case id ~ email => User(id, email)
    }
  }

  val token = {
      get[String]("token.token") ~
      get[Long]("token.userId") ~
      date("token.createdAt") map {
      case token ~ userId ~ createdAt => Token(token, userId, createdAt)
    }
  }
  // -- Queries
  User
  /**
    * Retrieve a emplofyee from the id.
    */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where email = {email}").on('email -> email).as(user.singleOpt)
    }
  }

  def getTokenByEmail(email: String): Option[Token] = {
    DB.withConnection { implicit connection =>
        SQL("select t.* from user u join token t on(u.email = {email} AND u.userId = t.userId)").on('email -> email)
          .as(token.singleOpt)
    }
  }

  /**
    * Retrieve all Users.
    *
    * @return
    */
  def findAll: List[User] = {
    DB.withConnection { implicit connection =>
      try {
        SQL("select * from user").as(user *)
      } catch {
        case ex: Exception => Logger.info("ERROR", ex); Nil
      }
    }
  }

  /**
    * Insert a new user.
    *
    * @param user The user values.
    */
  def insert(user: User): Option[Long] = {
    DB.withConnection { implicit connection =>
        SQL(
          """
          insert into user values (
    		 {userId}, {email}
          )
        """).on(
          'userId -> Option.empty[Long],
          'email -> user.email).executeInsert()
    }
  }

  def saveToken(userId: Long, token: String): Option[Long] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into token values (
    		  {userId}, {token}, {createdAt}
          )
        """).on(
        'userId -> userId,
        'token -> token,
        'createdAt -> Calendar.getInstance().getTime()).executeInsert()
    }
  }

}
