package com.owlunit

import akka.actor.Actor
import akka.event.Logging._
import spray.routing.directives.LogEntry
import spray.http._
import spray.routing._
import StatusCodes._
import spray.http.CacheDirectives._
import spray.http.HttpHeaders._
import spray.httpx.TwirlSupport._
import html._

class MyServiceActor extends Actor with MyService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)

}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService { // with ServerSideEventsDirectives {

//  val sseProcessor = actorRefFactory.actorOf(Props { new Actor {
//    def receive = {
//      case (channel: ActorRef, lastEventID: Option[String]) =>
//        // Print LastEventID if present
//        lastEventID.foreach(lei => println(s"LastEventID: $lei"))
//
//        // Simulate some work
//        Thread.sleep(3000)
//
//        channel ! Message("some\ndata\ntest", Some("customEvent"), Some("someId"))
//    }
//  }})

  val myRoute =
    host("localhost", "127.0.0.1") {

//      pathPrefix("sse") {
//        respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
//          sse { (channel, lastEventID) =>
//          // Register a closed event handler
//            channel ! RegisterClosedHandler( () => println("Connection closed !!!") )
//
//            // Use the channel
//            sseProcessor ! (channel, lastEventID)
//          }
//        }
//      } ~
      path("favicon.ico") {
        complete(NotFound) // fail early in order to prevent error response logging
      } ~
      logRequestResponse(showErrorResponses _) {
        getFromResourceDirectory("static/") ~
        path("") {
          complete(index())
        }
      }
    }

  def respondAsEventStream =
    respondWithHeader(`Cache-Control`(`no-cache`)) &
      respondWithHeader(`Connection`("Keep-Alive")) &
      respondWithHeader(RawHeader("Content-Type", "text/event-stream"))

  def showRequest(request: HttpRequest) = LogEntry(request.uri, InfoLevel)

  def showErrorResponses(request: HttpRequest): Any => Option[LogEntry] = {
    case HttpResponse(OK, _, _, _)       => Some(LogEntry("200: " + request.uri, InfoLevel))
    case HttpResponse(NotFound, _, _, _) => Some(LogEntry("404: " + request.uri, WarningLevel))
    case r @ HttpResponse(Found | MovedPermanently, _, _, _) =>
      Some(LogEntry(s"${r.status.intValue}: ${request.uri} -> ${r.header[HttpHeaders.Location].map(_.uri.toString).getOrElse("")}", WarningLevel))
    case response ⇒ Some(
      LogEntry("Non-200 response for\n  Request : " + request + "\n  Response: " + response, WarningLevel))
  }

}