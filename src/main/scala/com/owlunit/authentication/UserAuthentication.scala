package com.owlunit.authentication

import scala.concurrent.Future
import spray.routing.{Route, HttpService, AuthorizationFailedRejection}
import com.owlunit.models.{FacebookSession, User}
import spray.routing.authentication.{Authentication, ContextAuthenticator}

import com.owlunit.lib.FacebookGraph
import spray.http.HttpCookie
import scala.util.{Failure, Success}


/**
 * DESCRIPTION PLACEHOLDER 
 */
trait UserAuthentication { self: HttpService =>
  import scala.concurrent.ExecutionContext.Implicits.global
  import org.json4s.DefaultFormats

  val cookieName = "cube_session"

  def signUserIn = parameters('token, 'id) { (token, id) =>
    FacebookGraph.basicUserInfo(token, "") onComplete {
      case Success(jvalue) => complete("The user was logged in")
      case Failure(reason) => reject(NoUserLoggedInRejection)
    }
  }


//  { ctx =>
//
//    val session: Option[FacebookSession] = for {
//      id     <- ctx.request.uri.query.get("id")
//      token  <- ctx.request.uri.query.get("token")
//      future <- FacebookGraph.basicUserInfo(token, "")
//      json   <- future
//      realId <- (json \ "id").extractOpt[String]
//      if realId == id
//    } yield {
//      val session = FacebookSession(id, token)
//      //todo: update or create user with this session
//      session
//    }
//
//    if (session.isDefined)
//      setCookie(HttpCookie(cookieName, content = session.get.hash)) {
//        complete("The user was logged in")
//      }
//    else
//      reject(NoUserLoggedInRejection)
//
//  }

  def authenticateUser: ContextAuthenticator[User] = { ctx =>

    val id    = ctx.request.uri.query.get("id")
    val token = ctx.request.uri.query.get("token")

    doAuth(id, token)
  }

  private def doAuth(idOption: Option[String], tokenOption: Option[String]): Future[Authentication[User]] = {
    Future {

      val user = for {
        id    <- idOption
        token <- tokenOption
      } yield {
        User("name", "surname", FacebookSession("", ""))
      }

      Either.cond(user.isDefined, user.get, AuthorizationFailedRejection)
    }
  }
}
