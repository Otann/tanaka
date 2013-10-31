package cube.tanaka

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.{WriteConcern, MongoConnection}
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject

case class Omega(_id: ObjectId = new ObjectId, z: String, y: Boolean)

object Global {
  val connection = MongoConnection()
}


object OmegaDAO extends SalatDAO[Omega, ObjectId](collection = Global.connection("quick-salat")("omega"))

class TestRunner {
  def main(args: List[String]) {

    val o = Omega(z = "something", y = false)
//    val _id = OmegaDAO.insert(o)
//    val o_* = OmegaDAO.findOne(MongoDBObject("z" -> "something"))


//    val toUpdate = o.copy(z = "something else")
//    OmegaDAO.update(MongoDBObject("z" -> "something"), toUpdate, upsert = false, multi = false, WriteConcern.Normal)
//    val o_** = OmegaDAO.findOneById(new ObjectId("4dac7b3e75e1b63949139c91"))

  }
}
