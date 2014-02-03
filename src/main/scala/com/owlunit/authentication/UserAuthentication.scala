package com.owlunit.authentication

import scala.concurrent.{ExecutionContext, Future}
import spray.routing.{HttpService, Route, AuthorizationFailedRejection}
import spray.routing.authentication.{Authentication, ContextAuthenticator}
import spray.http.{DateTime, HttpCookie}
import scala.util.{Failure, Success}
import akka.event.LoggingAdapter

import com.owlunit.models._
import com.owlunit.lib.FacebookGraph
import spray.util.LoggingContext

/**
 * Route and directive to authenticate and login user
 *
 * @author anton
 */
trait UserAuthentication { self: HttpService =>

  private val cookieIdName     = "cube_fb_id"
  private val cookieSecretName = "cube_secret"

  val facebookAPI = FacebookGraph //TODO: FacebookGraph should be injected for testing purposes
  implicit val formats = org.json4s.DefaultFormats

  /**
   * Checks if passed token and facebookId is valid by reaching /me endpoint in FacebookGraph
   */
  def loginUser(implicit executor: ExecutionContext, logger: LoggingContext): Route =
    parameters('token, 'id) { (token, id) =>
    // set fields to empty, because all we need is id
      onComplete(facebookAPI.basicUserInfo(token, fields = "")) {

        case Success(info) =>
          // safely extract id from json and compare with passed parameter
          val passesIdIsCorrect: Option[Boolean] = (info \ "id").extractOpt[String].map(_ == id)

          passesIdIsCorrect match {
            case Some(true) =>
              // create new session for user
              val session = FacebookSession(id, token)

              // block until new session is ensured with db
              UserDAO.ensureUser(info, token)

              // prepare cookies
              val expires = Some(DateTime.now + 1000 * 60 * 60 * 24)
              val cookieId = HttpCookie(cookieIdName, session.facebookId, expires = expires)
              val cookieSecret = HttpCookie(cookieSecretName, session.secret, expires = expires)

              // respond with cookies
              setCookie(cookieId, cookieSecret) {
                complete("logged in")
              }

            case Some(false) =>
              logger.warning(s"Somebody tried to authorize mismatched facebookId $id and facebookToken $token")
              reject(FacebookTokenMismatch)

            case None =>
              logger.error(s"Cant recognize response from Facebook: $info")
              reject(FacebookFail)
          }

        case Failure(reason) =>
          logger.error(s"Failed to access FacebookAPI with token $token")
          reject(FacebookFail)
      }
    }


  /**
   * Wraps another method to become ContextAuthenticator
   */
  def authenticateUser(implicit executor: ExecutionContext): ContextAuthenticator[User] = { ctx => doAuth(ctx.request.cookies)(executor) }

  /**
   * Performs actual user extraction based on cookies
   * Cookies are set by [[com.owlunit.authentication.UserAuthentication.loginUser]] route
   *
   * @param cookies cookies from request
   * @return delayed result
   */
  private def doAuth(cookies: List[HttpCookie])(implicit executor: ExecutionContext): Future[Authentication[User]] = {
    Future {

      val user = for {
        fbIdCookie   <- cookies.find(_.name == cookieIdName)
        secretCookie <- cookies.find(_.name == cookieSecretName)
        user         <- UserDAO.findBySession(fbIdCookie.content, secretCookie.content)
      } yield user

      Either.cond(user.isDefined, user.get, AuthorizationFailedRejection)
    }
  }
}