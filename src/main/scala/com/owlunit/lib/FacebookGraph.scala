package com.owlunit.lib

import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import dispatch._
import scala.util.{Success, Try, Failure}
import org.json4s.JValue

/**
 * Accessor to Facebook Graph API
 * Deals with plain data, doesn't not deal with domain classes
 */
object FacebookGraph extends DispatchHelper {

  val config = ConfigFactory.load()

  val host      = config.getString("tanaka.host")
  val key       = config.getString("tanaka.facebook.key")
  val secret    = config.getString("tanaka.facebook.secret")
  val scope     = config.getString("tanaka.facebook.scope")
  val locale    = config.getString("tanaka.facebook.locale")
  val callback  = host + "/api/facebook/auth"

  def oauthUrl(csrf: String) = s"https://www.facebook.com/dialog/oauth?client_id=$key&redirect_uri=$callback&state=csrf&scope=$scope"

  def baseHost = :/("graph.facebook.com").secure <<? Map("locale" -> locale)

  /**
   * Request an access token from facebook for given code
   *
   * @param code from facebook
   * @return AccessToken
   */
  def accessToken(csrf: String, code: String): Future[Try[AccessToken]] = {

    val req = baseHost / "oauth" / "access_token" <<? Map(
      "client_id"     -> key,
      "client_secret" -> secret,
      "redirect_uri"  -> callback,
      "code" -> code,
      "state" -> csrf,
      "scope" -> scope
    )

    doRequest[AccessToken](req) { out =>
      // make map from `access_token=XXX&expires=XXX&...`
      val tokenParams = out.split("&").map { param =>
        val pair = param.split("=")
        (pair(0), pair(1))
      }.toMap

      // Generate AccessToken
      (tokenParams.get("access_token"), tokenParams.get("expires")) match {

        case (Some(token), Some(exp)) =>
          Try(AccessToken(token, code, (new DateTime).plusSeconds(exp.toInt)))

        case _ =>
          Failure(new IllegalArgumentException("Can't receive access_token or expiration time"))
      }
    }
  }

  /**
   * Make a request with the access token as a parameter
   *
   * @param req request
   * @param token token
   * @return JValue
   */
  private def completeOauthReq(req: Req, token: String): Future[Try[JValue]] =
    completeJsonRequest(req <<? Map("access_token" -> token))


  /**
   * Returns public info about user by FacebookId
   * @param id facebookId
   * @return json with values
   */
  def publicUserInfo(id: String, fields: String): Future[Try[JValue]] =
    completeJsonRequest(baseHost / id <<? Map("fields" -> fields))

  /**
   * Fetches basic info about user from facebook with given permissions from props
   *
   * @param token user's token
   * @param fields requested fields
   * @return info about user in json
   */
  def basicUserInfo(token: String, fields: String): Future[Try[JValue]] =
    completeOauthReq(baseHost / "me" <<? Map("fields" -> fields), token)


}

case class AccessToken(token: String, code: String, expires: DateTime) {
  def isExpired: Boolean = expires.isBefore(new DateTime)
}