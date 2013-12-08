package com.owlunit.models

import com.novus.salat.global._
import com.novus.salat.annotations.raw.Persist
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.WriteConcern
import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import com.owlunit.lib.md5

/**
 * Fresh start for user model
 */
case class User(fullName      : String = "",
                firstName     : String = "",
                lastName      : String = "",
                photos        : UserPhotoUrls = UserPhotoUrls(),
                facebook      : FacebookAuthInfo)


case class UserPhotoUrls(main: String = "http://placehold.it/150x150",
                         backdrop: String = "http://placehold.it/150x150")

case class FacebookAuthInfo(id: String, token: String) {
  // Save secret hash to DB
  @Persist val hash = calculate(id, token)

  // Add salt to hide plain md5
  val salt = "FutureDoesNotExistsYet"

  // define how hash is calculated
  def calculate(facebookId: String, facebookToken: String) = md5(facebookToken + facebookToken + salt)

  // allow to check any other id and token
  def check(facebookId: String, facebookToken: String) = hash == calculate(facebookId, facebookToken)
}

object UserDAO extends SalatDAO[User, ObjectId](db("user.users")) {

  def findOrCreate(facebookId: String, token: String) = this.findByFacebook(facebookId) match {
    case Some(user) =>
      user
    case None =>
      val user = User(facebook = FacebookAuthInfo(facebookId, token))
      this.save(user, WriteConcern.Majority)
      user
  }

  def findByFacebook(facebookId: String) = this.findOne(MongoDBObject("facebook.id" -> facebookId))

}


