package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

import scala.language.postfixOps
import play.api.Logger

case class User(id: Long, email: String)
case class Token(id: Long, token: String)


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
    get[Long]("token.userId") ~
      get[String]("token.token")  map {
      case id ~ token => Token(id, token)
    }
  }
  // -- Queries
  User
  /**
    * Retrieve a employee from the id.
    */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users where email = {email}").on('email -> email).as(user.singleOpt)
    }
  }

  def getTokenByEmail(email: String): Option[Token] = {
    DB.withConnection { implicit connection =>
      SQL("select t.* from users u join token t on(u.email = {email} AND u.userId = t.userId)").on('email -> email).as(token.singleOpt)
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
        SQL("select * from users").as(user *)
      } catch {
        case ex: Exception => Logger.info("ERROR", ex); Nil
      }
    }
  }

  override def toString = super.toString

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

}
