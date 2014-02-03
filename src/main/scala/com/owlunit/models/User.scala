package com.owlunit.models

import com.novus.salat.global._
import com.novus.salat.annotations.raw.{Key, Persist}
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.Imports._
import scala.util.Try
import org.json4s.JValue

/**
 * Fresh start for user model
 */
case class User(name      : String,
                session   : FacebookSession,
                @Key("_id") id: ObjectId = new ObjectId())


abstract class UserDAOAbstract(collection: MongoCollection) extends SalatDAO[User, ObjectId](collection) {

  implicit val formats = org.json4s.DefaultFormats

  /**
   * Made to authorize user by data passed from browser
   * Correct pair of parameters is written on each user's login
   */
  def findBySession(facebookId: String, sessionSecret: String): Option[User] = {
    this.findOne(MongoDBObject("session.facebookId" -> facebookId, "session.secret" -> sessionSecret))
  }

  /**
   * Searches user by his facebook id.
   * If found, updates required fields with data from facebook
   * Otherwise creates new user
   *
   * NB: updates session too
   */
  def ensureUser(info: JValue, facebookToken: String, wc: WriteConcern = WriteConcern.Safe): Try[User] = Try {
    val facebookId = (info \ "id").extract[String]
    val session = FacebookSession(facebookId, facebookToken)

    val user = this.findOne(MongoDBObject("session.facebookId" -> facebookId)) match {
      case Some(found) => found.copy(
        name    = (info \ "name").extract[String],
        session = FacebookSession(facebookId, facebookToken)
      )
      case None => User(
        name    = (info \ "name").extract[String],
        session = session
      )
    }
    this.save(user, wc)

    user
  }

}

object UserDAO extends UserDAOAbstract(db("user"))

/**
 * Stores facebook info (persistent id and temporal token) about user
 * Constructs hashed secret to use it as "public key" for authorization
 *
 * @param facebookId id from facebook
 * @param token token to access FacebookAPIs
 */
case class FacebookSession(facebookId: String, token: String) {

  val salt = "FuckMeInTheIO"

  @Persist val secret = md5(facebookId + salt)

  //TODO: move to some lib package
  def md5(s: String) = {
    val m = java.security.MessageDigest.getInstance("MD5")
    val b = s.getBytes("UTF-8")
    m.update(b, 0, b.length)
    new java.math.BigInteger(1, m.digest()).toString(16)
  }

}

