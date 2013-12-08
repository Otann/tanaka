package com.owlunit

import com.mongodb.casbah.MongoConnection

package object models {

  val connection = MongoConnection()
  val db = connection("salat-demo")

}
