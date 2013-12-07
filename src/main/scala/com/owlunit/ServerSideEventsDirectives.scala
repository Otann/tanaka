//package com.owlunit
//
//import spray.routing._
//import spray.routing.Directives._
//import spray.http._
//import spray.http.HttpHeaders._
//import akka.actor._
//
//// Enable scala features
////import scala.language.postfixOps
//import scala.language.implicitConversions
//
///**
// * This code is port of https://github.com/siriux/spray_sse with minor improvements
// */
//trait ServerSideEventsDirectives {
//
//  def sse(body: (ActorRef, Option[String]) => Unit)(implicit refFactory: ActorRefFactory): Route = {
//
//    // TODO These headers should be standard headers
//    val preflightHeaders = List(
//      RawHeader("Access-Control-Allow-Methods", "GET"),
//      RawHeader("Access-Control-Allow-Headers", "Last-Event-ID, Cache-Control"),
//      RawHeader("Access-Control-Max-Age", "86400")
//    )
//
//    def lastEventId =
//      optionalHeaderValueByName("Last-Event-ID") | parameter("lastEventId"?)
//
//    def sseRoute(lei: Option[String]) = (ctx: RequestContext) => {
//      val connectionHandler = refFactory.actorOf(SSEHandler.props(ctx))
//      body(connectionHandler, lei)
//    }
//
//    get {
//      respondWithHeader(RawHeader("Content-Type", "text/event-stream")) { // TODO This should be a standard media type
//        lastEventId { lei =>
//          sseRoute(lei)
//        }
//      }
//    } ~
//    // Answer preflight requests. Needed for Yaffle
//    method(HttpMethods.OPTIONS) {  // TODO Change this with options, that it's included in Master
//      respondWithHeaders(preflightHeaders: _*) {
//        complete(StatusCodes.OK)
//      }
//    }
//
//  }
//}
//
//object ServerSideEventsDirectives extends ServerSideEventsDirectives
