package com.owlunit.models

import com.novus.salat.global._
import com.novus.salat.annotations.raw.{Key, Persist}
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.owlunit.lib.md5
import com.mongodb.casbah.Imports._

/**
 * Fresh start for user model
 */
case class User(firstName : String,
                lastName  : String,
                session   : FacebookSession,
                @Key("_id") id: ObjectId = new ObjectId())


abstract class UserDAOAbstract(collection: MongoCollection) extends SalatDAO[User, ObjectId](collection) {

  def findByFacebook(facebookId: String) = this.findOne(MongoDBObject("facebook.id" -> facebookId))
  
}

object UserDAO extends UserDAOAbstract(db("user"))

case class FacebookSession(id: String, token: String) {
  @Persist val hash = calculate(id, token)

  // Add salt to hide plain md5
  val salt = "FutureDoesNotExistsYet"

  // define how hash is calculated
  def calculate(facebookId: String, facebookToken: String) = md5(facebookToken + facebookToken + salt)

  // allow to check any other id and token
  def check(facebookId: String, facebookToken: String) = hash == calculate(facebookId, facebookToken)
}


