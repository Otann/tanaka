package com.owlunit.sse

import spray.routing._
import spray.routing.Directives._
import akka.actor._

/**
 * This code is port of https://github.com/siriux/spray_sse with minor improvements
 *
 * Provides directive that wraps route with ServerSideEvent handler
 * Brings reference to SSEHandler actor and id of last message to route scope
*/
trait SSEDirectives {

  def sse(body: (ActorRef, Option[String]) => Unit)(implicit refFactory: ActorRefFactory): Route = {

    def lastEventIdDirective = optionalHeaderValueByName("Last-Event-ID") | parameter("lastEventId"?)

    def sseRoute(lastEventId: Option[String]) = (ctx: RequestContext) => {
      val connectionHandler = refFactory.actorOf(SSEHandler.props(ctx))
      body(connectionHandler, lastEventId)
    }

    get {
      lastEventIdDirective { lastEventId =>
        sseRoute(lastEventId)
      }
    }

  }
}

object SSEDirectives extends SSEDirectives
