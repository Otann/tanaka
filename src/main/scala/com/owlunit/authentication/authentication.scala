package com.owlunit

import spray.routing.{Rejection, RequestContext}
import scala.concurrent.Future
import com.mongodb.casbah.MongoConnection

package object authentication {

  type ContextAuthenticator[T] = RequestContext => Future[Authentication[T]]
  type Authentication[T] = Either[Rejection, T]

}
