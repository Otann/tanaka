package com.owlunit

import akka.actor.{Props, ActorRef, Actor}
import akka.event.Logging._
import spray.routing.directives.LogEntry
import spray.http._
import spray.routing._
import StatusCodes._
import spray.http.CacheDirectives._
import spray.http.HttpHeaders._
import spray.httpx.TwirlSupport._
import html._
import spray.routing.Directives._
import spray.http.HttpRequest
import spray.http.HttpHeaders.RawHeader
import scala.Some
import spray.http.HttpResponse
import scala.concurrent.duration._
import com.owlunit.sse.{SSEHandler, SSEDirectives}

class MyServiceActor extends Actor with MyService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)

}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService with SSEDirectives {
  import SSEHandler._

  val sseProcessor = actorRefFactory.actorOf(Props { new Actor {
    implicit val ec = akka.dispatch.ExecutionContexts.global()
    def receive = {
      case (channel: ActorRef, _) =>
        // Simulate some work
        for(msgId <- 1 to 10) {
          context.system.scheduler.scheduleOnce(msgId.seconds, channel, Message((msgId * 10).toString))
        }

    }
  }})

  val myRoute =
    host("localhost", "127.0.0.1") {

      pathPrefix("sse") {
        respondWithHeader(`Cache-Control`(`no-cache`)) {
          sse { (channel, lastEventID) =>
          // Register a closed event handler
            channel ! RegisterClosedHandler( () => println("Connection closed !!!") )

            // Use the channel
            sseProcessor ! (channel, lastEventID)
          }
        }
      } ~
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

  def showErrorResponses(request: HttpRequest): Any => Option[LogEntry] = {
    case HttpResponse(OK, _, _, _)       => Some(LogEntry("200: " + request.uri, InfoLevel))
    case HttpResponse(NotFound, _, _, _) => Some(LogEntry("404: " + request.uri, WarningLevel))
    case response => Some(LogEntry("Non-200 response for\n  Request : " + request + "\n  Response: " + response, WarningLevel))
  }

}