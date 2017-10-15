package controllers

import java.util.concurrent.TimeoutException

import models._
import org.apache.commons.validator.routines.UrlValidator
import play.api.Play.current
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Promise
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._
import play.mvc.Http
import services.KeyGenerator

import scala.concurrent.Future
import scala.concurrent.duration._

case class LoginForm(email: String)

class Application extends Controller {

  implicit val timeout = 10.seconds
  implicit val empJsonFormat = Json.format[Bookmark]

  def index = Action{ request =>
    request.session.get("token").map { token =>
      Redirect("/dashboard")
    }.getOrElse {
      Ok(views.html.index("Your new application is ready."))
    }
  }

  def dashboard= Action { request =>
    request.session.get("token").map { token =>
      Ok(views.html.dashboard("Your new application is ready."))
    }.getOrElse {
      Redirect("/")
    }
  }

  /**
   * Describe the bookmark form (used in both edit and create screens).
   */
  val bookmarkForm = Form(
    mapping(
      "id" -> ignored(0: Long),
      "name" -> nonEmptyText,
      "url" -> nonEmptyText.verifying("Must be valid URL.", url => url.isEmpty || UrlValidator.getInstance().isValid(url)),
      "slug" -> ignored("":String))(Bookmark.apply)(Bookmark.unapply))

  val loginForm = Form(
    mapping(
      "email" -> nonEmptyText)(LoginForm.apply)(LoginForm.unapply))


  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.login)

  /**
   * Display the list of Bookmarks.
   */
  def list = Action.async { implicit request =>
    val token = getRequestToken(request);
    val futurePage: Future[List[Bookmark]] = TimeoutFuture(Bookmark.findAll(token))
    futurePage
      .map(bookmarks => Ok(Json.toJson(bookmarks)))
      .recover {
        case t: TimeoutException =>
        Logger.error("Problem found in bookmark list process")
        InternalServerError(t.getMessage)
      }
  }

  /**
    *  Redirect to URL by slug
    */
  def redirect(key:String) = Action.async { implicit request =>
    val futurePage: Future[Option[Bookmark]] = TimeoutFuture(Bookmark.findBySlug(key))
    futurePage.map{
          case Some(bookmark) => MovedPermanently(bookmark.url)
          case None           => NotFound
    }.recover {
      case t: TimeoutException =>
        Logger.error("Problem found in bookmark lookup process")
        InternalServerError(t.getMessage)
    }
  }

  /**
    *  Login user
    */
  def login = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest("")),
      loginForm => {
        val futureUpdateEmp: Future[Option[Token]] = TimeoutFuture(User.getTokenByEmail(loginForm.email))
        futureUpdateEmp.map {
          case Some(token) => {
            Redirect("/dashboard").withSession(request.session + ("token" -> token.token))
          }
          case None           => NotFound //TODO
        }.recover {
          case t: TimeoutException =>
            Logger.error("Problem found in employee update process")
            InternalServerError(t.getMessage)
        }
    })
  }


  def logout = Action{ implicit request =>
    Redirect("/").withSession(request.session - "token")
  }

  /**
    * Get the authorization token from the request, either from cookie or
    * Authorization header
    */
  def getRequestToken(request: Request[AnyContent]): String = {
    var token : String = null;
    if (!request.session.get("token").isEmpty){
      token = request.session.get("token").get;
    }
    if (token == null) {
      var authHeader = request.headers.get(Http.HeaderNames.AUTHORIZATION).get
      if (authHeader.contains("Bearer")) {
        token = authHeader.split(" ")(1);
      }
    }
    token
  }

  /**
   * Handle the 'new bookmark form' submission.
   */
  def save = Action.async { implicit request =>
    val token = getRequestToken(request)

    bookmarkForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      bookmark => {
        val slug = KeyGenerator.generateSlug(bookmark.url);
        if (slug.isEmpty) {
          Logger.error("Couldn't create slug")
          InternalServerError("Couldn't create slug")
        }

        val futureUpdateBookmark: Future[Option[Long]] = TimeoutFuture(Bookmark.insert(bookmark, slug, token))
        futureUpdateBookmark.map {
          case Some(bookmarkId) =>
            val msg = s"Bookmark ${bookmark.name} has been created"
            Logger.info(msg)
            val json = JsObject(Seq(
              "name" -> JsString(bookmark.name),
              "url" -> JsString(bookmark.url),
              "slug" -> JsString(slug)
            ))
            Ok(json)
          case None =>
            val msg = s"Bookmark ${bookmark.name} has not created"
            Logger.info(msg)
            val json = JsObject(Seq(
              "error" -> JsString("Not created")
            ))
            Conflict(json)
        }.recover {
          case t: TimeoutException =>
            Logger.error("Problem found in bookmark update process")
            val json = JsObject(Seq(
              "error" -> JsString("Not created")
            ))
            InternalServerError(json)
        }
      })
  }

  /**
   * Handle bookmark deletion.
   */
  def delete(id: Long) = Action.async {
    val futureInt = TimeoutFuture(Bookmark.delete(id))
    futureInt.map(i => Home.flashing("success" -> "Employee has been deleted")).recover {
      case t: TimeoutException =>
        Logger.error("Problem deleting employee")
        InternalServerError(t.getMessage)
    }
  }
}


object TimeoutFuture {

  def apply[A](block: => A)(implicit timeout: FiniteDuration): Future[A] = {

    val promise = scala.concurrent.Promise[A]()

    // if the promise doesn't have a value yet then this completes the future with a failure
    Promise.timeout(Nil, timeout).map(_ => promise.tryFailure(new TimeoutException("This operation timed out")))

    // this tries to complete the future with the value from block
    Future(promise.success(block))

    promise.future
  }

}