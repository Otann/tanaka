package com.owlunit.authentication

import com.typesafe.config.ConfigFactory
import scala.concurrent.Future
import spray.routing.AuthorizationFailedRejection
import scala.concurrent.ExecutionContext.Implicits.global
import com.owlunit.authentication.{Authentication, ContextAuthenticator}
import com.owlunit.models.{UserDAO, User}


/**
 * DESCRIPTION PLACEHOLDER 
 */
trait UserAuthentication {

  val conf = ConfigFactory.load()
  lazy val configusername = conf.getString("security.username")
  lazy val configpassword = conf.getString("security.password")


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
        UserDAO.findOrCreate(id, token)
      }

      Either.cond(user.isDefined, user.get, AuthorizationFailedRejection)
    }
  }
}
